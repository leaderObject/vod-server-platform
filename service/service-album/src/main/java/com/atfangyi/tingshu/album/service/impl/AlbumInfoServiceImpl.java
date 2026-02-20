package com.atfangyi.tingshu.album.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.mapper.*;
import com.atfangyi.tingshu.album.service.AlbumInfoService;
import com.atfangyi.tingshu.common.ParamAssert.ServiceAssert;
import com.atfangyi.tingshu.common.annotation.Cache;
import com.atfangyi.tingshu.common.constant.KafkaConstant;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.service.KafkaService;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.album.AlbumAttributeValue;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.album.BaseAttributeValue;
import com.atfangyi.tingshu.model.search.AlbumInfoIndex;
import com.atfangyi.tingshu.model.search.AttributeValueIndex;
import com.atfangyi.tingshu.query.album.AlbumInfoQuery;
import com.atfangyi.tingshu.vo.album.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

    @Autowired
    private AlbumInfoMapper albumInfoMapper;

    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;

    @Autowired
    private AlbumStatMapper albumStatMapper;

    @Autowired
    private BaseAttributeValueMapper attributeValueMapper;

    @Autowired
    private BaseAttributeMapper baseAttributeMapper;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean saveAlbumInfo(AlbumInfoVo albumInfoVo) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        boolean flag = false;
        if (albumInfoVo != null) {
            AlbumInfo albumInfo = new AlbumInfo();
            BeanUtils.copyProperties(albumInfoVo, albumInfo);
            int insert = albumInfoMapper.insert(albumInfo);
            boolean saveAlbumAttributeValue = albumAttributeValueMapper.saveAlbumAttributeValue(albumInfoVo.getAlbumAttributeValueVoList(), albumInfo.getId());
            Integer statNum = albumStatMapper.InitAlbumStatNum(albumInfo.getId(), Arrays.asList(SystemConstant.ALBUM_STAT_PLAY, SystemConstant.ALBUM_STAT_SUBSCRIBE, SystemConstant.ALBUM_STAT_BUY, SystemConstant.ALBUM_STAT_COMMENT));
            if (insert > 0 && saveAlbumAttributeValue && statNum > 0) {
                flag = !flag;
                bloomFilter.add(albumInfo.getId());
            }
            log.info("保存专辑信息：{}", flag);
        }
        return flag;
    }

    @Override
    public IPage<AlbumListVo> findUserAlbumPage(Long page, Long limit, AlbumInfoQuery albumInfoQuery) {
        /**
         * 当前wrapper暂时没有条件后续做调整
         */
        LambdaQueryWrapper<AlbumInfo> albumInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        albumInfoLambdaQueryWrapper.eq(AlbumInfo::getUserId, AuthContextHolder.getUserId());
        albumInfoLambdaQueryWrapper.orderByDesc(AlbumInfo::getCreateTime);
        Page<AlbumInfo> albumInfoPage = albumInfoMapper.selectPage(new Page<>(page, limit), albumInfoLambdaQueryWrapper);
        Page<AlbumListVo> albumListVoPage = new Page<>();
        BeanUtils.copyProperties(albumInfoPage, albumListVoPage);
        albumListVoPage.setRecords(albumInfoPage.getRecords().stream().map(albumInfo -> {
            AlbumListVo albumListVo = new AlbumListVo();
            BeanUtils.copyProperties(albumInfo, albumListVo);
            albumListVo.setAlbumId(albumInfo.getId());
            albumListVo.setAlbumCommentStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_COMMENT));
            albumListVo.setBuyStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_BUY));
            albumListVo.setSubscribeStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_SUBSCRIBE));
            albumListVo.setPlayStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_PLAY));
            return albumListVo;
        }).collect(Collectors.toList()));
        return albumListVoPage;

    }

    @Override
    @Cache(prefix = "AlbumInfo")
    public AlbumInfo getAlbumInfoById(Long id) {
        return albumInfoMapper.selectById(id);

    }


    private AlbumInfo getAlbumInfo(Long id) {
        AlbumInfo albumInfo = albumInfoMapper.selectById(id);
        if (albumInfo != null) {
            List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueMapper.selectList(Wrappers.lambdaQuery(AlbumAttributeValue.class).eq(AlbumAttributeValue::getAlbumId, albumInfo.getId()));
            if (!CollectionUtils.isEmpty(albumAttributeValues)) {
                albumInfo.setAlbumAttributeValueVoList(albumAttributeValues.stream().peek(albumAttributeValue -> {
                    albumAttributeValue.setAttributeName(baseAttributeMapper.selectById(albumAttributeValue.getAttributeId()).getAttributeName());
                    albumAttributeValue.setValueName(attributeValueMapper.selectById(albumAttributeValue.getValueId()).getValueName());
                }).collect(Collectors.toList()));
            }
        }
        return albumInfo;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateAlbumInfo(Long id, AlbumInfoVo albumInfoVo) {
        boolean flag = true;
        ServiceAssert.ParamAssert(albumInfoVo);
        AlbumInfo albumInfo = new AlbumInfo();
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        albumInfo.setId(id);
        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
        albumAttributeValueVoList.forEach(albumAttributeValueVo -> {
            LambdaQueryWrapper<AlbumAttributeValue> albumAttributeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
            AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
            albumAttributeValue.setValueId(albumAttributeValueVo.getValueId());
            albumAttributeValueLambdaQueryWrapper.eq(AlbumAttributeValue::getAttributeId, albumAttributeValueVo.getAttributeId()).eq(AlbumAttributeValue::getAlbumId, albumInfo.getId());
            if (albumAttributeValueMapper.update(albumAttributeValue, albumAttributeValueLambdaQueryWrapper) < 1) {
                log.error("更新专辑属性值失败：{}", albumAttributeValueVo);
                return;
            }
            if ("0".equals(albumInfoVo.getIsOpen())) {
                kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_LOWER, id.toString());
            } else {
                kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_UPPER, id.toString());
            }
        });

        return flag;
    }

    @Override
    public List<AlbumInfo> findUserAllAlbumList() {
        LambdaQueryWrapper<AlbumInfo> albumInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        albumInfoLambdaQueryWrapper.orderByDesc(AlbumInfo::getCreateTime);
        albumInfoLambdaQueryWrapper.select(AlbumInfo::getId, AlbumInfo::getAlbumTitle, AlbumInfo::getStatus);
        albumInfoLambdaQueryWrapper.last("limit 500");
        return albumInfoMapper.selectList(albumInfoLambdaQueryWrapper);
    }

    @Override
    public List<AlbumAttributeValue> findAlbumAttributeValue(Long albumId) {
        return albumAttributeValueMapper.selectList(Wrappers.lambdaQuery(AlbumAttributeValue.class).eq(AlbumAttributeValue::getAlbumId, albumId));
    }

    @Override
    @Cache(prefix = "AlbumStatVo")
    public AlbumStatVo getAlbumStatVo(Long albumId) {
        return albumInfoMapper.getAlbumStatVo(albumId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void receiver(TrackStatMqVo trackStatMqVo) {
        albumInfoMapper.updateAlbumStat(trackStatMqVo);
    }

    @Override
    public AlbumStatVo getTrackStatVo(Long trackId) {
        return albumInfoMapper.getTrackStatVo(trackId);
    }

    @Override
    public boolean updateAlbumInfoById(AlbumDto albumDto) {
        AlbumInfo albumInfo = albumInfoMapper.selectById(albumDto.getId());
        ServiceAssert.ObjectAssert(albumInfo);
        //进行数据的修改
        BeanUtils.copyProperties(albumDto, albumInfo);
        return albumInfoMapper.updateById(albumInfo) > 0;

    }


    /**
     * 专辑数据导入
     */
    @Override
    public List<AlbumInfoIndex> importAlbum() {
        List<AlbumInfo> albumInfos = albumInfoMapper.selectList(null);
        return albumInfos.stream().map(albumInfo -> {
            AlbumInfoIndex albumInfoIndex = BeanUtil.copyProperties(albumInfo, AlbumInfoIndex.class);
            albumInfoIndex.setCommentStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_COMMENT));
            albumInfoIndex.setBuyStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_BUY));
            albumInfoIndex.setSubscribeStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_SUBSCRIBE));
            albumInfoIndex.setPlayStatNum(albumStatMapper.queryAlbumStatNumber(albumInfo.getId(), SystemConstant.ALBUM_STAT_PLAY));
            albumInfoIndex.setHotScore(new Random().nextDouble(999.99));
            albumInfoIndex.setAttributeValueIndexList(albumAttributeValueMapper.selectList(Wrappers.lambdaQuery(AlbumAttributeValue.class)
                            .eq(AlbumAttributeValue::getAlbumId, albumInfo.getId())).stream().
                    map(albumAttributeValue -> BeanUtil.copyProperties(albumAttributeValue,
                            AttributeValueIndex.class)).collect(Collectors.toList()));
            return albumInfoIndex;
        }).collect(Collectors.toList());
    }
}
