package com.atfangyi.tingshu.user.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atfangyi.tingshu.album.AlbumFeignClient;
import com.atfangyi.tingshu.common.constant.KafkaConstant;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.common.service.KafkaService;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.common.util.MongoUtil;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.album.TrackInfo;
import com.atfangyi.tingshu.model.user.UserListenProcess;
import com.atfangyi.tingshu.user.service.UserListenProcessService;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.atfangyi.tingshu.vo.user.UserListenProcessVo;
import com.atfangyi.tingshu.vo.user.UserListenProcessListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Autowired
    private AlbumFeignClient albumFeignClient;


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
        query.addCriteria(Criteria.where("userId").is(AuthContextHolder.getUserId())
                .and("albumId").is(userListenProcessVo.getAlbumId())
                .and("trackId").is(userListenProcessVo.getTrackId()));
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

    @Override
    public IPage<UserListenProcessListVo> findUserPage(Long page, Long limit) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId);
        Query query = new Query(Criteria.where("userId").is(userId).and("isShow").is(1))
                .with(Sort.by(Sort.Direction.DESC, "updateTime"));
        Pageable pageable = PageRequest.of(page.intValue() - 1, limit.intValue());
        query.with(pageable);
        List<UserListenProcess> processList = mongoTemplate.find(query, UserListenProcess.class, collectionName);
        long count = mongoTemplate.count(new Query(Criteria.where("userId").is(userId).and("isShow").is(1)), UserListenProcess.class, collectionName);

        List<UserListenProcessListVo> voList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(processList)) {
            for (UserListenProcess process : processList) {
                UserListenProcessListVo vo = new UserListenProcessListVo();
                vo.setId(process.getId());
                vo.setAlbumId(process.getAlbumId());
                vo.setTrackId(process.getTrackId());
                vo.setBreakSecond(process.getBreakSecond());
                Result<TrackInfo> trackResult = albumFeignClient.getTrackInfoById(process.getTrackId());
                if (trackResult != null && trackResult.getData() != null) {
                    TrackInfo trackInfo = trackResult.getData();
                    vo.setTrackTitle(trackInfo.getTrackTitle());
                    vo.setMediaDuration(trackInfo.getMediaDuration());
                    if (trackInfo.getMediaDuration() != null && trackInfo.getMediaDuration().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal rate = process.getBreakSecond().divide(trackInfo.getMediaDuration(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                        vo.setPlayRate(rate.intValue() + "%");
                    }
                }
                Result<AlbumInfo> albumResult = albumFeignClient.getAlbumInfoById(process.getAlbumId());
                if (albumResult != null && albumResult.getData() != null) {
                    vo.setAlbumTitle(albumResult.getData().getAlbumTitle());
                    vo.setCoverUrl(albumResult.getData().getCoverUrl());
                }
                voList.add(vo);
            }
        }

        IPage<UserListenProcessListVo> pageResult = new Page<>(page, limit, count);
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public void deleteById(String id) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId);
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, UserListenProcess.class, collectionName);
    }

    @Override
    public UserListenProcessVo getLatelyTrack() {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId);
        Query query = new Query(Criteria.where("userId").is(userId).and("isShow").is(1))
                .with(Sort.by(Sort.Direction.DESC, "updateTime")).limit(1);
        UserListenProcess process = mongoTemplate.findOne(query, UserListenProcess.class, collectionName);
        if (process == null) {
            return null;
        }
        UserListenProcessVo vo = new UserListenProcessVo();
        vo.setAlbumId(process.getAlbumId());
        vo.setTrackId(process.getTrackId());
        vo.setBreakSecond(process.getBreakSecond());
        return vo;
    }
}
