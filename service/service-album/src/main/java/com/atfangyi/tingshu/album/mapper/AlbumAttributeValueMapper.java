package com.atfangyi.tingshu.album.mapper;

import com.atfangyi.tingshu.model.album.AlbumAttributeValue;
import com.atfangyi.tingshu.vo.album.AlbumAttributeValueVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface AlbumAttributeValueMapper extends BaseMapper<AlbumAttributeValue> {

    boolean saveAlbumAttributeValue(@Param("albumAttributeValueVoList")List<AlbumAttributeValueVo> albumAttributeValueVoList, @Param("id") Long id);


}
