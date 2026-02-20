package com.atfangyi.tingshu.album.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.mapper.TrackInfoMapper;
import com.atfangyi.tingshu.album.mapper.TrackStatMapper;
import com.atfangyi.tingshu.album.service.AlbumInfoService;
import com.atfangyi.tingshu.album.service.TrackInfoService;
import com.atfangyi.tingshu.album.service.VodService;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.album.TrackInfo;
import com.atfangyi.tingshu.model.album.TrackStat;
import com.atfangyi.tingshu.model.order.OrderInfo;
import com.atfangyi.tingshu.model.user.UserPaidTrack;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.order.client.OrderFeignClient;
import com.atfangyi.tingshu.user.client.UserFeignClient;
import com.atfangyi.tingshu.vo.album.AlbumTrackListVo;
import com.atfangyi.tingshu.vo.album.TrackInfoVo;
import com.atfangyi.tingshu.vo.album.TrackListVo;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import kotlin.jvm.internal.Lambda;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

    @Autowired
    private TrackInfoMapper trackInfoMapper;

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private TrackStatMapper trackStatMapper;

    @Autowired
    private VodService vodService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderFeignClient orderFeignClient;


    @Override
    public IPage<TrackListVo> findUserTrackPage(Long page, Long limit) {
        Page<TrackListVo> trackListVoPage = new Page<>();
        Page<TrackInfo> trackInfoPage = trackInfoMapper.selectPage(new Page<>(page, limit), Wrappers.lambdaQuery(TrackInfo.class).orderByDesc(TrackInfo::getCreateTime));
        BeanUtils.copyProperties(trackInfoPage, trackListVoPage);
        trackListVoPage.setRecords(trackInfoPage.getRecords().stream().map(trackInfo -> {
            TrackListVo trackListVo = new TrackListVo();
            trackListVo.setTrackId(trackInfo.getId());
            trackListVo.setTrackTitle(trackInfo.getTrackTitle());
            trackListVo.setCoverUrl(trackInfo.getCoverUrl());
            trackListVo.setMediaDuration(trackInfo.getMediaDuration());
            trackListVo.setStatus(trackInfo.getStatus());
            trackListVo.setAlbumId(trackInfo.getAlbumId());
            AlbumInfo albumInfo = albumInfoService.getById(trackInfo.getAlbumId());
            trackListVo.setAlbumTitle(albumInfo != null ? albumInfo.getAlbumTitle() : "");
            trackListVo.setPlayStatNum(this.getTrackCommentStatNum(trackInfo.getId(), "0701"));
            trackListVo.setCollectStatNum(this.getTrackCommentStatNum(trackInfo.getId(), "0702"));
            trackListVo.setPraiseStatNum(this.getTrackCommentStatNum(trackInfo.getId(), "0703"));
            trackListVo.setAlbumCommentStatNum(this.getTrackCommentStatNum(trackInfo.getId(), "0704"));
            return trackListVo;
        }).collect(Collectors.toList()));
        return trackListVoPage;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveTrackInfo(TrackInfoVo trackInfoVo) {
        try {
            TrackInfo trackInfo = new TrackInfo();
            BeanUtils.copyProperties(trackInfoVo, trackInfo);
            JSONObject mediaDetailInfo = vodService.getMediaDetailInfo(trackInfoVo.getMediaFileId());
            if (mediaDetailInfo == null) {
                throw new RuntimeException("媒体文件不存在");
            }
            //metaData 信息
            JSONObject metaData = JSONObject.parseObject(mediaDetailInfo.get("metaData").toString());
            BigDecimal audioDuration = metaData.getBigDecimal("audioDuration");
            //basicInfo
            JSONObject basicInfo = JSONObject.parseObject(mediaDetailInfo.get("basicInfo").toString());
            String mediaUrl = basicInfo.getString("mediaUrl");
            String type = basicInfo.getString("type");
            Long size = metaData.getLong("size");
            trackInfo.setMediaDuration(audioDuration);
            trackInfo.setMediaUrl(mediaUrl);
            trackInfo.setMediaType(type);
            trackInfo.setMediaSize(size);
            trackInfo.setUserId(1L);
            trackInfo.setSource(SystemConstant.TRACK_SOURCE_UPLOAD);
            trackInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);
            trackInfoMapper.insert(trackInfo);
            //当前事务不能用this 业务spring事务基于 tx 必须使用动态代理中的对象不然事务传播行为不生效
            saveTrackStatInfo(trackInfo.getId());
        } catch (RuntimeException e) {
            log.error("保存声音信息失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public IPage<AlbumTrackListVo> findAlbumTrackPage(Long albumId, Long page, Long limit) {
        IPage<AlbumTrackListVo> albumTrackListVoIPage = trackInfoMapper.selectPageByAlbumId(new Page<AlbumTrackListVo>(page, limit), albumId);
        //
        UserVipService userVipService = userFeignClient.userVipService();
        //判断用户是否购买过该专辑
        if (userFeignClient.isPaidAlbum(albumId).getData()
                || (userVipService != null && userVipService.getExpireTime().after(DateTime.now().toDate()))) {
            albumTrackListVoIPage.getRecords().stream().forEach(albumTrackListVo -> albumTrackListVo.setIsShowPaidMark(false));
            return albumTrackListVoIPage;
        }
        //不为Vip用户查询用户所购买的专辑专辑声音
        LambdaQueryWrapper<TrackInfo> trackInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        trackInfoLambdaQueryWrapper.eq(TrackInfo::getAlbumId, albumId).eq(TrackInfo::getUserId, AuthContextHolder.getUserId());
        List<TrackInfo> trackInfos = trackInfoMapper.selectList(trackInfoLambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(trackInfos)) {
            List<Long> collect = trackInfos.stream().map(s -> s.getId()).collect(Collectors.toList());
            Map<Long, Object> data = userFeignClient.isPaidTrack(collect).getData();
            albumTrackListVoIPage.getRecords().stream().forEach(albumTrackListVo -> {
                Integer i = Integer.valueOf(data.get(albumTrackListVo.getTrackId()).toString());
                albumTrackListVo.setIsShowPaidMark(i == 1 ? false : true);
            });

        }
        return albumTrackListVoIPage;
    }

    /**
     * 统计声音
     *
     * @param trackStatMqVo
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void receiver(TrackStatMqVo trackStatMqVo) {
        try {
            //防止消息被重复使用
            if (redisTemplate.opsForValue().setIfAbsent(trackStatMqVo.getBusinessNo(), trackStatMqVo.getTrackId(), 3L, TimeUnit.HOURS)) {
                boolean flag = trackInfoMapper.receiver(trackStatMqVo);

                if (SystemConstant.TRACK_STAT_PLAY.equals(trackStatMqVo.getTrackId())
                        || SystemConstant.TRACK_STAT_COMMENT.equals(trackStatMqVo.getTrackId()))
                    albumInfoService.receiver(trackStatMqVo);
            }
        } catch (Exception e) {
            redisTemplate.delete(trackStatMqVo.getBusinessNo());
            throw new RuntimeException(e);
        }
    }

    @Override
    public IPage<AlbumTrackListVo> queryTrackInfoByAlbumId(Long id, Long page, Long limit) {
        return trackInfoMapper.selectPageByCondition(new Page<TrackListVo>(), id);
    }

    @Override
    public boolean deleteTrackInfoById(Long id) {
        List<Long> longs = orderFeignClient.queryOrderInfoByItemType(SystemConstant.ORDER_ITEM_TYPE_TRACK, SystemConstant.ORDER_STATUS_PAID).getData();
        if (longs.contains(id)) {
            throw new GuiguException(501, "该声音已经购买 暂时不能删除");
        }
        return trackInfoMapper.deleteById(id) > 0;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    private void saveTrackStatInfo(Long id) {
        trackStatMapper.saveTrackStatInfo(id, Arrays.asList(
                SystemConstant.TRACK_STAT_PLAY,
                SystemConstant.TRACK_STAT_COLLECT,
                SystemConstant.TRACK_STAT_PRAISE,
                SystemConstant.TRACK_STAT_COMMENT
        ));

    }


    public Integer getTrackCommentStatNum(Long trackId, String statType) {
        LambdaQueryWrapper<TrackStat> trackStatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        trackStatLambdaQueryWrapper.eq(TrackStat::getTrackId, trackId).eq(TrackStat::getStatType, statType);
        TrackStat trackStat = trackStatMapper.selectOne(trackStatLambdaQueryWrapper);
        return trackStat == null ? 0 : trackStat.getStatNum();
    }
}
