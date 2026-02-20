package com.atfangyi.tingshu.album.api;

import com.atfangyi.tingshu.album.service.AlbumInfoService;
import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.album.AlbumAttributeValue;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.query.album.AlbumInfoQuery;
import com.atfangyi.tingshu.vo.album.AlbumInfoVo;
import com.atfangyi.tingshu.vo.album.AlbumListVo;
import com.atfangyi.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Operation(summary = "新增专辑")
    @PostMapping("/albumInfo/saveAlbumInfo")
    public Result saveAlbumInfo(@RequestBody AlbumInfoVo albumInfoVo) {
        return albumInfoService.saveAlbumInfo(albumInfoVo) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "查看当前用户专辑分页列表")
    @PostMapping("/albumInfo/findUserAlbumPage/{page}/{limit}")
    @Login(required = true)
    public Result<IPage<AlbumListVo>> findUserAlbumPage(@PathVariable Long page,
                                                        @PathVariable Long limit,
                                                        @RequestBody(required = false) AlbumInfoQuery albumInfoQuery) {
        return Result.ok(albumInfoService.findUserAlbumPage(page, limit, albumInfoQuery));
    }

    @Operation(summary = "根据ID删除专辑")
    @DeleteMapping("/albumInfo/removeAlbumInfo/{id}")
    public Result removeAlbumInfo(@PathVariable Long id) {
        return albumInfoService.removeById(id) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "根据ID查询专辑信息")
    @GetMapping("/albumInfo/getAlbumInfo/{id}")
    public Result<AlbumInfo> getAlbumInfoById(@PathVariable Long id) {
        return Result.ok(albumInfoService.getAlbumInfoById(id));
    }

    @Operation(summary = "更新专辑信息")
    @PutMapping("/albumInfo/updateAlbumInfo/{id}")
    public Result updateAlbumInfo(@PathVariable Long id, @RequestBody AlbumInfoVo albumInfoVo) {
        return albumInfoService.updateAlbumInfo(id, albumInfoVo) ? Result.ok() : Result.fail();
    }

    @Operation(tags = "获取当前用户全部专辑列表")
    @GetMapping("/albumInfo/findUserAllAlbumList")
    public Result<List<AlbumInfo>> findUserAllAlbumList() {
        return Result.ok(albumInfoService.findUserAllAlbumList());
    }


    @Operation(summary = "获取专辑属性值列表")
    @GetMapping("/albumInfo/findAlbumAttributeValue/{albumId}")
    public Result<List<AlbumAttributeValue>> findAlbumAttributeValue(@PathVariable Long albumId) {
        return Result.ok(albumInfoService.findAlbumAttributeValue(albumId));
    }

    @Operation(summary = "根据专辑ID获取专辑统计信息")
    @GetMapping("/albumInfo/getAlbumStatVo/{albumId}")
    public AlbumStatVo getAlbumStatVo(@PathVariable Long albumId) {
        return albumInfoService.getAlbumStatVo(albumId);
    }

    @Operation(summary = "获取声音统计信息")
    @GetMapping("/trackInfo/getTrackStatVo/{trackId}")
    public Result<AlbumStatVo> getTrackStatVo(@PathVariable Long trackId) {
        return Result.ok(albumInfoService.getTrackStatVo(trackId));
    }

    @PostMapping("/updateAlbumInfoById")
    @Operation(summary = "根据id删除专辑")
    public boolean updateAlbumInfoById(@RequestBody AlbumDto albumDto) {
        return albumInfoService.updateAlbumInfoById(albumDto);
    }


}

