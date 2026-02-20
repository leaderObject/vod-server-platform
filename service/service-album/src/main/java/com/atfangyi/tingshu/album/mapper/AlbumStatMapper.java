package com.atfangyi.tingshu.album.mapper;

import com.atfangyi.tingshu.model.album.AlbumStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface AlbumStatMapper extends BaseMapper<AlbumStat> {


    Integer queryAlbumStatNumber(Long AlbumInfo, String statType);

    Integer  InitAlbumStatNum(@Param("id") Long id, @Param("list") List<String> list);

}
