package com.atfangyi.tingshu.album.impl;


import com.atfangyi.tingshu.album.AlbumFeignClient;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.album.*;
import com.atfangyi.tingshu.vo.album.AlbumStatVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Component
@Slf4j
public class AlbumDegradeFeignClient implements FallbackFactory<AlbumFeignClient> {
    @Override
    public AlbumFeignClient create(Throwable cause) {
        return new AlbumFeignClient() {
            @Override
            public Result<AlbumInfo> getAlbumInfoById(Long id) {
                log.error("getAlbumInfoById   异常原因{}", cause.getMessage());
                return null;
            }

            @Override
            public Result<List<AlbumAttributeValue>> findAlbumAttributeValue(Long albumId) {
                log.error("findAlbumAttributeValue   异常原因{}", cause.getMessage());
                return null;
            }

            @Override
            public Result<BaseCategoryView> getCategoryView(Long category3Id) {
                log.error("getCategoryView   异常原因{}", cause.getMessage());
                return null;
            }

            @Override
            public Result<List<BaseCategory3>> findTopBaseCategory3(Long category1Id) {
                log.error("findTopBaseCategory3   异常原因{}", cause.getMessage());
                return null;
            }

            @Override
            public AlbumStatVo getAlbumStatVo(Long albumId) {
                log.error("getAlbumStatVo   异常原因{}", cause.getMessage());
                return null;
            }

            @Override
            public List<BaseCategory1> queryAllCategoryInfo() {
                log.error("queryAllCategoryInfo   异常原因{}", cause.getMessage());
                return List.of();
            }

            @Override
            public Long findAlbumTrackCount() {
                log.error("findAlbumTrackPage   异常原因{}", cause.getMessage());
                return 0L;
            }

            @Override
            public Result<TrackInfo> getTrackInfoById(Long id) {
                log.error("getTrackInfoById   异常原因{}", cause.getMessage());
                return null;
            }

            @Override
            public boolean updateAlbumInfoById(AlbumDto albumDto) {
                log.error("远程dto ---{}", albumDto);
                log.error("updateAlbumInfoById   降级处理 --{}", cause.getMessage());
                return false;
            }
        };
    }

}
