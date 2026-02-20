package com.atfangyi.tingshu.album.service;

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.album.AlbumAttributeValue;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.search.AlbumInfoIndex;
import com.atfangyi.tingshu.query.album.AlbumInfoQuery;
import com.atfangyi.tingshu.vo.album.AlbumInfoVo;
import com.atfangyi.tingshu.vo.album.AlbumListVo;
import com.atfangyi.tingshu.vo.album.AlbumStatVo;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlbumInfoService extends IService<AlbumInfo> {


    boolean saveAlbumInfo(AlbumInfoVo albumInfoVo);


    IPage<AlbumListVo> findUserAlbumPage(Long page, Long limit, AlbumInfoQuery albumInfoQuery);


    AlbumInfo getAlbumInfoById(Long id);

    boolean updateAlbumInfo(Long id,AlbumInfoVo albumInfoVo);

    List<AlbumInfo> findUserAllAlbumList();

    List<AlbumAttributeValue> findAlbumAttributeValue(Long albumId);

    AlbumStatVo getAlbumStatVo(Long albumId);

    void receiver(TrackStatMqVo trackStatMqVo);

    AlbumStatVo getTrackStatVo(Long trackId);

    boolean updateAlbumInfoById(AlbumDto albumDto);

    List<AlbumInfoIndex> importAlbum();


}
