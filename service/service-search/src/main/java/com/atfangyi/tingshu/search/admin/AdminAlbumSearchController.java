package com.atfangyi.tingshu.search.admin;


import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.SearchDto;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/search")
@Tag(name = "阅读后台管理端接口")
public class AdminAlbumSearchController {

    @Resource
    private SearchService searchService;


    @PostMapping("/queryAlbumDetailCount")
    @Operation(summary = "查询最近数据趋势")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "queryAlbumDetailCount")
    public Result<Map<String, Object>> queryAlbumDetailCount() {
        return Result.ok(searchService.queryAlbumDetailCount());
    }

    @PostMapping("/queryAlbumPageByCondition/{page}/{size}")
    @Operation(summary = "根据条件查询搜索")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "queryAlbumPageByCondition")
    public Result<Map<String, Object>> queryAlbumPageByCondition(@RequestBody SearchDto searchDto, @PathVariable Integer page, @PathVariable Integer size) {
        return Result.ok(searchService.queryAlbumPageByCondition(searchDto, page, size));
    }

//    @PostMapping("/AlbumUpdateById")
//    @Operation(summary = "修改查询查询内容")
//    public Result AlbumUpdateById(@PostMapping  ) {
//      return
//    }

    @DeleteMapping("/removeAlbumById/{id}")
    @Operation(summary = "删除专辑")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "2", operatorMethod = "removeAlbumById")
    public Result removeAlbumById(@PathVariable Long id) {
        return searchService.removeAlbumById(id) ? Result.ok() : Result.fail();
    }


    @PostMapping("/updateAlbumInfoById")
    @Operation(summary = "修改专辑信息")
    @OperatorLogAnnotation(operatorType = "3", operatorMethod = "updateAlbumInfoById")
    @AdminLogin
    public Result updateAlbumInfoById(@RequestBody AlbumDto albumDto) {
        return searchService.updateAlbumInfoById(albumDto) ? Result.ok() : Result.fail();
    }

}
