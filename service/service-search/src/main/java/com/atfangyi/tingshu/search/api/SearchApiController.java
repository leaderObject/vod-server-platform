package com.atfangyi.tingshu.search.api;

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.album.BaseCategory3;
import com.atfangyi.tingshu.model.search.AlbumInfoIndex;
import com.atfangyi.tingshu.query.search.AlbumIndexQuery;
import com.atfangyi.tingshu.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "搜索专辑管理")
@RestController
@RequestMapping("api/search")
@SuppressWarnings({"all"})
public class SearchApiController {
    private static Long TemporaryCategory1Id = 1L;
    @Autowired
    private SearchService searchService;


    @Operation(summary = "上架专辑-导入索引库")
    @GetMapping("/albumInfo/upperAlbum/{albumId}")
    public Result upperAlbum(@PathVariable Long albumId) {
        return searchService.upperAlbum(albumId) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "专辑检索")
    @PostMapping("/albumInfo")
    public Result albumInfo(@RequestBody AlbumIndexQuery albumIndexQuery) {
        return Result.ok(searchService.albumInfo(albumIndexQuery));
    }

    @Operation(summary = "查询指定一级分类下热门排行专辑")
    @GetMapping("/albumInfo/channel/{category1Id}")
    public Result<List<Map<String, Object>>> channel(@PathVariable Long category1Id) {
        return Result.ok(searchService.channel(category1Id));
    }

    @Operation(summary = "关键字自动补全")
    @GetMapping("/albumInfo/completeSuggest/{keyword}")
    public Result<List<String>> completeSuggest(@PathVariable String keyword) {
        return Result.ok(searchService.completeSuggest(keyword));
    }


    @Operation(summary = "查询专辑 详情")
    @GetMapping("/albumInfo/{albumId}")
    public Result<Map<String, Object>> albumInfo(@PathVariable Long albumId) {
        return Result.ok(searchService.queryAlbumInfo(albumId));
    }

    @Operation(summary = "更新所有分类下排行榜-手动调用")
    @GetMapping("/albumInfo/updateLatelyAlbumRanking")
    public Result updateLatelyAlbumRanking() {
        searchService.updateLatelyAlbumRanking();
        return Result.ok();
    }

    @Operation(summary = "获取排行榜")
    @GetMapping("/albumInfo/findRankingList/{category1Id}/{dimension}")
    public Result<List<AlbumInfoIndex>> findRankingList(@PathVariable String category1Id, @PathVariable String dimension) {
        if (!"undefined".equals(category1Id)) {
            TemporaryCategory1Id = Long.valueOf(category1Id);
        }
        return Result.ok(searchService.findRankingList(TemporaryCategory1Id, dimension));
    }
}

