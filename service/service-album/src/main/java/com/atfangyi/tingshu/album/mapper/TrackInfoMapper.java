package com.atfangyi.tingshu.album.mapper;

import com.atfangyi.tingshu.model.album.TrackInfo;
import com.atfangyi.tingshu.vo.album.AlbumTrackListVo;
import com.atfangyi.tingshu.vo.album.TrackListVo;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


public interface TrackInfoMapper extends BaseMapper<TrackInfo> {


    IPage<AlbumTrackListVo> selectPageByAlbumId(Page<AlbumTrackListVo> objectPage, @Param("id") Long albumId);

    boolean receiver(@Param("trackStatMqVo") TrackStatMqVo trackStatMqVo);


    Page<AlbumTrackListVo> selectPageByCondition(Page<TrackListVo> trackListVoPage, Long id);

}
