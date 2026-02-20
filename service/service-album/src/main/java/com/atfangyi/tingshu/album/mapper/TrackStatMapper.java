package com.atfangyi.tingshu.album.mapper;

import com.atfangyi.tingshu.model.album.TrackStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface TrackStatMapper extends BaseMapper<TrackStat> {


    void saveTrackStatInfo(@Param("id") Long id,@Param("list") List<String> list);

}
