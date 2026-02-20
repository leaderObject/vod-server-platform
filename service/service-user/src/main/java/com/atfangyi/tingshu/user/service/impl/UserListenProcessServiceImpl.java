package com.atfangyi.tingshu.user.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.common.constant.KafkaConstant;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.service.KafkaService;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.common.util.MongoUtil;
import com.atfangyi.tingshu.model.user.UserListenProcess;
import com.atfangyi.tingshu.user.service.UserListenProcessService;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.atfangyi.tingshu.vo.user.UserListenProcessVo;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings({"all"})
public class UserListenProcessServiceImpl implements UserListenProcessService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KafkaService kafkaService;


    @Override
    public BigDecimal getTrackBreakSecondBytrackId(Long trackId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(AuthContextHolder.getUserId()).where("trackId").is(trackId));
        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class,
                MongoUtil
                        .getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, AuthContextHolder.getUserId()));
        return userListenProcess == null ? new BigDecimal("0.00") : userListenProcess.getBreakSecond();
    }


    /**
     * 大多数业务做的都是在用户登录的情况下
     *
     * @param
     */
    @Override
    public void updateListenProcess(UserListenProcessVo userListenProcessVo) {
        Query query = new Query();
        query.addCriteria(Criteria.where("albumId").is(userListenProcessVo.getAlbumId())
                .where("trackId").is(userListenProcessVo.getTrackId()));
        UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil
                .getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, AuthContextHolder.getUserId()));
        if (userListenProcess == null) {
            userListenProcess = new UserListenProcess();
            //第一次暂停
            userListenProcess.setUserId(AuthContextHolder.getUserId());
            userListenProcess.setAlbumId(userListenProcessVo.getAlbumId());
            userListenProcess.setTrackId(userListenProcessVo.getTrackId());
            userListenProcess.setIsShow(1);
            userListenProcess.setUpdateTime(new Date());
            userListenProcess.setCreateTime(new Date());
            userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());

        } else {
            //跟进用户暂停时间
            userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
            userListenProcess.setUpdateTime(new Date());
        }
        /**
         * 设置专辑播放次数
         * 1.用户不能频繁播放并且设置播放次数  // 每个用户每天只能增加一次播放专辑次数
         */
        if (redisTemplate.opsForValue().setIfAbsent(RedisConstant.USER_TRACK_REPEAT_STAT_PREFIX + userListenProcessVo.getTrackId(),
                userListenProcessVo.getTrackId(),
                DateUtil.endOfDay(new Date()).getTime() - System.currentTimeMillis()
                , TimeUnit.SECONDS)) {
            TrackStatMqVo trackStatMqVo = new TrackStatMqVo();
            trackStatMqVo.setStatType(SystemConstant.TRACK_STAT_PLAY);
            trackStatMqVo.setCount(1);
            trackStatMqVo.setTrackId(userListenProcessVo.getTrackId());
            trackStatMqVo.setBusinessNo(IdUtil.fastSimpleUUID());
            trackStatMqVo.setAlbumId(userListenProcessVo.getAlbumId());
            kafkaService.sendMessage(KafkaConstant.QUEUE_TRACK_STAT_UPDATE, JSONObject.toJSONString(trackStatMqVo));
        }

        mongoTemplate.save(userListenProcess, MongoUtil
                .getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, AuthContextHolder.getUserId()));
    }
}
