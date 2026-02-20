package com.atfangyi.tingshu.album.admin;


import com.atfangyi.tingshu.album.service.TrackInfoService;
import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.vo.album.AlbumTrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@SuppressWarnings({"all"})
@RequestMapping("/admin/album")
@Tag(name = "声音管理模块")
public class AdminTrackInfoController {

    @Resource
    private TrackInfoService trackInfoService;

    @Operation(summary = "获取专辑对应的声音")
    @PostMapping("/queryTrackInfoByAlbumId/{id}/{page}/{limit}")
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "queryTrackInfoByAlbumId")
    public Result<IPage<AlbumTrackListVo>> queryTrackInfoByAlbumId(@PathVariable Long id, @PathVariable Long page, @PathVariable Long limit) {
        if (limit > 100) {
        }//接口盗刷暂时不做处理
        return Result.ok(trackInfoService.queryTrackInfoByAlbumId(id, page, limit));
    }

    @Operation(summary = "根据Id删除声音")
    @DeleteMapping("/deleteTrackInfoById/{id}")
    @OperatorLogAnnotation(operatorType = "2", operatorMethod = "deleteTrackInfoById")
    @AdminLogin
    public Result deleteTrackInfoById(@PathVariable Long id) {
        return trackInfoService.deleteTrackInfoById(id) ? Result.ok() : Result.fail();
    }
}
