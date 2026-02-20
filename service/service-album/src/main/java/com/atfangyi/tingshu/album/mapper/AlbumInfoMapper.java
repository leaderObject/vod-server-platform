package com.atfangyi.tingshu.album.mapper;

import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.vo.album.AlbumStatVo;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;


public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {

    AlbumStatVo getAlbumStatVo(Long albumId);


    void updateAlbumStat(@Param("trackStatMqVo") TrackStatMqVo trackStatMqVo);

    AlbumStatVo getTrackStatVo(Long trackId);

}
