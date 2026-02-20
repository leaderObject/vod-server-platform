package com.atfangyi.tingshu.album;

import com.atfangyi.tingshu.album.impl.AlbumDegradeFeignClient;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.album.*;
import com.atfangyi.tingshu.vo.album.AlbumStatVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 * 专辑模块远程调用Feign接口
 * </p>
 *
 * @author atguigu
 */
@FeignClient(value = "service-album", path = "api/album", fallbackFactory = AlbumDegradeFeignClient.class)
public interface AlbumFeignClient {


    @GetMapping("/albumInfo/getAlbumInfo/{id}")
    Result<AlbumInfo> getAlbumInfoById(@PathVariable Long id);


    @GetMapping("/albumInfo/findAlbumAttributeValue/{albumId}")
    Result<List<AlbumAttributeValue>> findAlbumAttributeValue(@PathVariable Long albumId);


    @GetMapping("/category/getCategoryView/{category3Id}")
    Result<BaseCategoryView> getCategoryView(@PathVariable Long category3Id);


    @GetMapping("/category/findTopBaseCategory3/{category1Id}")
    Result<List<BaseCategory3>> findTopBaseCategory3(@PathVariable Long category1Id);

    @GetMapping("/albumInfo/getAlbumStatVo/{albumId}")
    AlbumStatVo getAlbumStatVo(@PathVariable Long albumId);

    @GetMapping("/queryAllCategoryInfo")
    List<BaseCategory1> queryAllCategoryInfo();


    @GetMapping("/trackInfo/count")
    Long findAlbumTrackCount();

    @PostMapping("/updateAlbumInfoById")
    boolean updateAlbumInfoById(@RequestBody AlbumDto albumDto);


}
