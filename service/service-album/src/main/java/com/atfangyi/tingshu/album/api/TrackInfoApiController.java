package com.atfangyi.tingshu.album.api;

import com.atfangyi.tingshu.album.service.TrackInfoService;
import com.atfangyi.tingshu.album.service.VodService;
import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.album.TrackInfo;
import com.atfangyi.tingshu.query.album.TrackInfoQuery;
import com.atfangyi.tingshu.vo.album.AlbumTrackListVo;
import com.atfangyi.tingshu.vo.album.TrackInfoVo;
import com.atfangyi.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album")
@SuppressWarnings({"all"})
public class TrackInfoApiController {

    @Autowired
    private TrackInfoService trackInfoService;

    @Autowired
    private VodService vodService;


    @Operation(summary = "获取当前用户声音分页列表")
    @PostMapping("/trackInfo/findUserTrackPage/{page}/{limit}")
    public Result<IPage<TrackListVo>> findUserTrackPage(@PathVariable Long page, @PathVariable Long limit) {
        return Result.ok(trackInfoService.findUserTrackPage(page, limit));
    }


    @Operation(summary = "删除声音信息")
    @DeleteMapping("/trackInfo/removeTrackInfo/{id}")
    public Result removeTrackInfo(@PathVariable Long id) {
        return trackInfoService.removeById(id) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "获取声音信息")
    @GetMapping("/trackInfo/getTrackInfo/{id}")
    public Result<TrackInfo> getTrackInfo(@PathVariable Long id) {
        return Result.ok(trackInfoService.getById(id));
    }

    @Operation(summary = "上传声音")
    @PostMapping("/trackInfo/uploadTrack")
    public Result<Map<String, Object>> uploadTrack(MultipartFile file) {
        return Result.ok(vodService.uploadTrack(file));
    }

    @Operation(summary = "保存声音")
    @PostMapping("/trackInfo/saveTrackInfo")
    public Result saveTrackInfo(@RequestBody TrackInfoVo trackInfoVo) {
        return trackInfoService.saveTrackInfo(trackInfoVo) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "查询专辑声音分页列表")
    @GetMapping("/trackInfo/findAlbumTrackPage/{albumId}/{page}/{limit}")
    @Login
    public Result<IPage<AlbumTrackListVo>> findAlbumTrackPage(@PathVariable Long albumId, @PathVariable Long page, @PathVariable Long limit) {
        return Result.ok(trackInfoService.findAlbumTrackPage(albumId, page, limit));
    }

    @Operation(summary = "获取章节总数")
    @GetMapping("/trackInfo/count")
    public Long findAlbumTrackCount() {
        return trackInfoService.count();
    }


}

