package com.atfangyi.tingshu.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.AlbumFeignClient;
import com.atfangyi.tingshu.common.annotation.Cache;
import com.atfangyi.tingshu.common.constant.KafkaConstant;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.service.KafkaService;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.common.util.MongoUtil;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.album.TrackInfo;
import com.atfangyi.tingshu.model.user.*;
import com.atfangyi.tingshu.user.factory.UserPaidFactory;
import com.atfangyi.tingshu.user.mapper.UserInfoMapper;
import com.atfangyi.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atfangyi.tingshu.user.mapper.UserPaidTrackMapper;
import com.atfangyi.tingshu.user.mapper.UserVipServiceMapper;
import com.atfangyi.tingshu.user.service.UserInfoService;
import com.atfangyi.tingshu.user.strategy.UserPaidStrategy;
import com.atfangyi.tingshu.vo.user.UserInfoVo;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import com.atfangyi.tingshu.vo.user.UserSubscribeVo;
import com.atfangyi.tingshu.vo.user.UserCollectVo;
import com.atfangyi.tingshu.vo.album.AlbumStatMqVo;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserPaidAlbumMapper userPaidAlbumMapper;

    @Autowired
    private UserPaidTrackMapper userPaidTrackMapper;

    @Autowired
    private UserPaidFactory userPaidFactory;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserVipServiceMapper userVipServiceMapper;


    @Override
    public Map<String, Object> wxLogin(String code) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            if (sessionInfo == null) {
                log.error("[用户服务]获取微信小程序OpenId失败");
                throw new GuiguException(ResultCodeEnum.DATA_ERROR);
            }
            //获取小程序OpenId
            String openid = sessionInfo.getOpenid();
            LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userInfoLambdaQueryWrapper.eq(UserInfo::getWxOpenId, openid);
            UserInfo userInfo = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);
            if (userInfo == null) {
                //用户首次登录
                userInfo = new UserInfo();
                userInfo.setWxOpenId(openid);
                userInfo.setNickname("坤友" + IdUtil.randomUUID().toString().replaceAll("-", ""));
                userInfo.setAvatarUrl("https://glsx.oss-cn-beijing.aliyuncs.com/OIP-C.webp");
                if (userInfoMapper.insert(userInfo) > 0) {
                    kafkaService.sendMessage(KafkaConstant.QUEUE_USER_REGISTER, userInfo.getId().toString());
                }
            }
            String snowflakeNextIdStr = IdUtil.getSnowflakeNextIdStr();
            redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + snowflakeNextIdStr, userInfo);
            map.put("token", snowflakeNextIdStr);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    @Override
    public UserInfo getUserInfo(String token) {
        Object object = redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        UserInfo userInfo = null;

        if (object != null) {
            userInfo = (UserInfo) object;
            // 检查用户是否为VIP会员
            userInfo.setIsVip(checkUserVipStatus(userInfo.getId()) ? 1 : 0);
        } else {
            userInfo = new UserInfo();
        }

        return userInfo;
    }

    /**
     * 检查用户是否为VIP会员
     */
    private boolean checkUserVipStatus(Long userId) {
        if (userId == null) return false;
        LambdaQueryWrapper<UserVipService> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVipService::getUserId, userId);
        wrapper.gt(UserVipService::getExpireTime, new Date()); // 过期时间大于当前时间
        return userVipServiceMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean updateUser(UserInfoVo userInfoVo, String token) {
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoVo, userInfo);
        userInfo.setId(AuthContextHolder.getUserId());
        boolean flag = userInfoMapper.updateById(userInfo) > 0;
        if (flag) {
            redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, userInfoMapper.selectById(userInfo.getId()));
        }
        return flag;
    }

    @Override
    public Boolean isPaidAlbum(Long albumId) {
        LambdaQueryWrapper<UserPaidAlbum> userPaidAlbumLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPaidAlbumLambdaQueryWrapper.eq(UserPaidAlbum::getAlbumId, albumId).eq(UserPaidAlbum::getUserId, AuthContextHolder.getUserId());
        return userPaidAlbumMapper.selectCount(userPaidAlbumLambdaQueryWrapper) > 0;

    }

    @Override
    public Map<Long, Object> isPaidTrack(List<Long> ids) {
        HashMap<Long, Object> map = new HashMap<>();
        LambdaQueryWrapper<UserPaidTrack> trackInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        trackInfoLambdaQueryWrapper.eq(UserPaidTrack::getUserId, AuthContextHolder.getUserId());
        trackInfoLambdaQueryWrapper.in(UserPaidTrack::getTrackId, ids);
        List<UserPaidTrack> userPaidTracks = userPaidTrackMapper.selectList(trackInfoLambdaQueryWrapper);
        //开启前六章免费试听环节
        if (CollectionUtils.isEmpty(userPaidTracks) && ids.size() >= 6) {
            for (int i = 0; i < ids.size(); i++) {
                if (i >= 6) {
                    map.put(ids.get(i), 0);
                } else {
                    map.put(ids.get(i), 1);
                }
            }
            return map;
        }
        List<Long> collect = userPaidTracks.stream().map(s -> s.getTrackId()).collect(Collectors.toList());
        for (Long id : ids) {
            map.put(id, collect.contains(id) ? 1 : 0);
        }
        return map;
    }

    @Override
    @Cache(prefix = "UserInfo")
    public UserInfo queryUserInfoByUserId(Long userId) {
        return userInfoMapper.selectById(userId);
    }

    @Override
    public void saveUserPaid(UserPaidRecordVo userPaidRecordVo) {
        UserPaidStrategy userPaidStrategy = userPaidFactory.getUserPaidStrategy(userPaidRecordVo.getItemType());
        userPaidStrategy.saveUserPaid(userPaidRecordVo);
    }

    @Override
    public UserVipService userVipService() {
        LambdaQueryWrapper<UserVipService> userVipServiceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userVipServiceLambdaQueryWrapper.eq(UserVipService::getUserId, AuthContextHolder.getUserId());
        userVipServiceLambdaQueryWrapper.orderByDesc(UserVipService::getExpireTime);
        userVipServiceLambdaQueryWrapper.last("limit 1");
        return userVipServiceMapper.selectOne(userVipServiceLambdaQueryWrapper);
    }

    @Override
    @Transactional
    public void queryUserVipStatus() {
        LambdaQueryWrapper<UserVipService> userVipServiceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userVipServiceLambdaQueryWrapper.select(UserVipService::getUserId);
        userVipServiceLambdaQueryWrapper.eq(UserVipService::getExpireTime, new DateTime().minusDays(1).toString("yyyy-MM-dd"));
        userVipServiceLambdaQueryWrapper.groupBy(UserVipService::getUserId);
        List<UserVipService> userVipServices = userVipServiceMapper.selectList(userVipServiceLambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(userVipServices)) {
            List<UserInfo> userInfos = userInfoMapper.selectList(Wrappers.lambdaQuery(UserInfo.class).in(UserInfo::getId, userVipServices.stream().map(userVipService -> userVipService.getUserId()).collect(Collectors.toList())));
            for (UserInfo userInfo : userInfos) {
                userInfo.setIsVip(0);
                userInfoMapper.updateById(userInfo);
            }
//        }
        }
    }

    @Override
    public List<UserInfo> queryAllUserInfo() {
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.select(UserInfo::getId, UserInfo::getPhone, UserInfo::getNickname, UserInfo::getAvatarUrl, UserInfo::getIsVip, UserInfo::getGender, UserInfo::getStatus, UserInfo::getCreateTime);
        return userInfoService.list(userInfoLambdaQueryWrapper);
    }

    @Override
    public UserInfo queryUserInfoById(Long id) {
        UserInfo userInfo = userInfoMapper.selectById(id);
        if (userInfo == null) throw new GuiguException(500, "用户不存在");
        return userInfo;
    }

    @Override
    public IPage<UserSubscribeVo> findUserSubscribePage(Long page, Long limit) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_SUBSCRIBE, userId);
        Query query = new Query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC, "createTime"));
        Pageable pageable = PageRequest.of(page.intValue() - 1, limit.intValue());
        query.with(pageable);
        List<UserSubscribe> userSubscribeList = mongoTemplate.find(query, UserSubscribe.class, collectionName);
        long count = mongoTemplate.count(new Query(Criteria.where("userId").is(userId)), UserSubscribe.class, collectionName);

        List<UserSubscribeVo> voList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userSubscribeList)) {
            for (UserSubscribe userSubscribe : userSubscribeList) {
                UserSubscribeVo vo = new UserSubscribeVo();
                vo.setAlbumId(userSubscribe.getAlbumId());
                vo.setCreateTime(userSubscribe.getCreateTime());
                Result<AlbumInfo> albumResult = albumFeignClient.getAlbumInfoById(userSubscribe.getAlbumId());
                if (albumResult != null && albumResult.getData() != null) {
                    AlbumInfo albumInfo = albumResult.getData();
                    vo.setAlbumTitle(albumInfo.getAlbumTitle());
                    vo.setCoverUrl(albumInfo.getCoverUrl());
                    vo.setIsFinished(albumInfo.getIsFinished());
                }
                voList.add(vo);
            }
        }

        IPage<UserSubscribeVo> pageResult = new Page<>(page, limit, count);
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public Boolean subscribe(Long albumId) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_SUBSCRIBE, userId);
        Query query = new Query(Criteria.where("userId").is(userId).and("albumId").is(albumId));
        UserSubscribe exist = mongoTemplate.findOne(query, UserSubscribe.class, collectionName);
        if (exist != null) {
            mongoTemplate.remove(query, UserSubscribe.class, collectionName);
            // 取消订阅 - 发送 -1 消息
            AlbumStatMqVo albumStatMqVo = new AlbumStatMqVo();
            albumStatMqVo.setAlbumId(albumId);
            albumStatMqVo.setStatType(SystemConstant.ALBUM_STAT_SUBSCRIBE);
            albumStatMqVo.setCount(-1);
            albumStatMqVo.setBusinessNo(IdUtil.fastSimpleUUID());
            kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_STAT_UPDATE, JSONObject.toJSONString(albumStatMqVo));
            return false;
        } else {
            UserSubscribe userSubscribe = new UserSubscribe();
            userSubscribe.setId(IdUtil.getSnowflakeNextIdStr());
            userSubscribe.setUserId(userId);
            userSubscribe.setAlbumId(albumId);
            userSubscribe.setCreateTime(new Date());
            mongoTemplate.save(userSubscribe, collectionName);
            // 订阅 - 发送 +1 消息
            AlbumStatMqVo albumStatMqVo = new AlbumStatMqVo();
            albumStatMqVo.setAlbumId(albumId);
            albumStatMqVo.setStatType(SystemConstant.ALBUM_STAT_SUBSCRIBE);
            albumStatMqVo.setCount(1);
            albumStatMqVo.setBusinessNo(IdUtil.fastSimpleUUID());
            kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_STAT_UPDATE, JSONObject.toJSONString(albumStatMqVo));
            return true;
        }
    }

    @Override
    public Boolean isSubscribe(Long albumId) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_SUBSCRIBE, userId);
        Query query = new Query(Criteria.where("userId").is(userId).and("albumId").is(albumId));
        UserSubscribe exist = mongoTemplate.findOne(query, UserSubscribe.class, collectionName);
        return exist != null;
    }

    @Override
    public IPage<UserCollectVo> findUserCollectPage(Long page, Long limit) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_COLLECT, userId);
        Query query = new Query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC, "createTime"));
        Pageable pageable = PageRequest.of(page.intValue() - 1, limit.intValue());
        query.with(pageable);
        List<UserCollect> userCollectList = mongoTemplate.find(query, UserCollect.class, collectionName);
        long count = mongoTemplate.count(new Query(Criteria.where("userId").is(userId)), UserCollect.class, collectionName);

        List<UserCollectVo> voList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userCollectList)) {
            for (UserCollect userCollect : userCollectList) {
                UserCollectVo vo = new UserCollectVo();
                vo.setTrackId(userCollect.getTrackId());
                vo.setCreateTime(userCollect.getCreateTime());
                Result<TrackInfo> trackResult = albumFeignClient.getTrackInfoById(userCollect.getTrackId());
                if (trackResult != null && trackResult.getData() != null) {
                    TrackInfo trackInfo = trackResult.getData();
                    vo.setTrackTitle(trackInfo.getTrackTitle());
                    vo.setAlbumId(trackInfo.getAlbumId());
                    Result<AlbumInfo> albumResult = albumFeignClient.getAlbumInfoById(trackInfo.getAlbumId());
                    if (albumResult != null && albumResult.getData() != null) {
                        vo.setCoverUrl(albumResult.getData().getCoverUrl());
                    }
                }
                voList.add(vo);
            }
        }

        IPage<UserCollectVo> pageResult = new Page<>(page, limit, count);
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public Boolean collect(Long trackId) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_COLLECT, userId);
        Query query = new Query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        UserCollect exist = mongoTemplate.findOne(query, UserCollect.class, collectionName);
        if (exist != null) {
            mongoTemplate.remove(query, UserCollect.class, collectionName);
            return false;
        } else {
            UserCollect userCollect = new UserCollect();
            userCollect.setId(IdUtil.getSnowflakeNextIdStr());
            userCollect.setUserId(userId);
            userCollect.setTrackId(trackId);
            userCollect.setCreateTime(new Date());
            mongoTemplate.save(userCollect, collectionName);
            return true;
        }
    }

    @Override
    public Boolean isCollect(Long trackId) {
        Long userId = AuthContextHolder.getUserId();
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_COLLECT, userId);
        Query query = new Query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        UserCollect exist = mongoTemplate.findOne(query, UserCollect.class, collectionName);
        return exist != null;
    }

    @Override
    public List<UserVipService> getVipServiceList(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserVipService> wrapper) {
        return userVipServiceMapper.selectList(wrapper);
    }

    @Override
    public long getVipServiceCount(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserVipService> wrapper) {
        return userVipServiceMapper.selectCount(wrapper);
    }
}
