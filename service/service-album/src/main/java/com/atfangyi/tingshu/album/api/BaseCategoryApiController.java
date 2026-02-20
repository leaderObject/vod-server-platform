package com.atfangyi.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.mapper.BaseCategory1Mapper;
import com.atfangyi.tingshu.album.mapper.BaseCategory3Mapper;
import com.atfangyi.tingshu.album.service.BaseCategoryService;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.album.BaseCategory1;
import com.atfangyi.tingshu.model.album.BaseCategory3;
import com.atfangyi.tingshu.model.album.BaseCategoryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value = "/api/album")
@SuppressWarnings({"all"})
public class BaseCategoryApiController {

    @Autowired
    private BaseCategoryService baseCategoryService;

    @Autowired
    private BaseCategory1Mapper   baseCategory1Mapper;


    @Operation(summary = "查询所有分类（1、2、3级分类）")
    @GetMapping("/category/getBaseCategoryList")
    public Result getBaseCategoryList() {
        return Result.ok(baseCategoryService.getBaseCategoryList());
    }

    @Operation(summary = "根据一级分类Id获取分类属性（标签）列表")
    @GetMapping("/category/findAttribute/{category1Id}")
    public Result categoryFindAttribute(@PathVariable Long category1Id) {
        return Result.ok(baseCategoryService.categoryFindAttribute(category1Id));
    }

    @Operation(summary = "根据三级分类Id 获取到分类信息")
    @GetMapping("/category/getCategoryView/{category3Id}")
    public Result<BaseCategoryView> getCategoryView(@PathVariable Long category3Id) {
        return Result.ok(baseCategoryService.getCategoryView(category3Id));
    }

    @Operation(summary = "根据一级分类Id查询三级分类列表")
    @GetMapping("/category/findTopBaseCategory3/{category1Id}")
    public Result<List<BaseCategory3>> findTopBaseCategory3(@PathVariable Long category1Id) {
        return Result.ok(baseCategoryService.findTopBaseCategory3(category1Id));
    }

    @Operation(summary = "根据一级分类id获取全部分类信息")
    @GetMapping("/category/getBaseCategoryList/{category1Id}")
    public Result getBaseCategoryList(@PathVariable Long category1Id) {
        return Result.ok(baseCategoryService.getBaseCategoryListBycategory1Id(category1Id));
    }

    @Operation(summary = "查询所有的分类数据")
    @GetMapping("/queryAllCategoryInfo")
    public   List<BaseCategory1> queryAllCategoryInfo(){
         return  baseCategory1Mapper.selectList(null);


    }
}

