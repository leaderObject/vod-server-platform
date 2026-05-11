package com.atfangyi.tingshu.album.service;

import com.atfangyi.tingshu.model.album.TrackInfo;
import com.atfangyi.tingshu.vo.album.AlbumTrackListVo;
import com.atfangyi.tingshu.vo.album.TrackInfoVo;
import com.atfangyi.tingshu.vo.album.TrackListVo;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TrackInfoService extends IService<TrackInfo> {

    IPage<TrackListVo> findUserTrackPage(Long page, Long limit);


    boolean saveTrackInfo(TrackInfoVo trackInfoVo);


    IPage<AlbumTrackListVo> findAlbumTrackPage(Long albumId, Long page, Long limit);


    void receiver(TrackStatMqVo trackStatMqVo);


    IPage<AlbumTrackListVo> queryTrackInfoByAlbumId(Long id, Long page, Long limit);


    boolean deleteTrackInfoById(Long id);

    boolean updateTrackInfo(TrackInfoVo trackInfoVo);

}
