package com.atfangyi.tingshu.album.admin;


import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.mapper.BaseCategory1Mapper;
import com.atfangyi.tingshu.album.mapper.BaseCategory2Mapper;
import com.atfangyi.tingshu.album.mapper.BaseCategory3Mapper;
import com.atfangyi.tingshu.album.service.AlbumInfoService;
import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.album.BaseCategory1;
import com.atfangyi.tingshu.model.album.BaseCategory2;
import com.atfangyi.tingshu.model.search.AlbumInfoIndex;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/album")
@SuppressWarnings({"all"})
@Tag(name = "管理专辑模块")
public class AminAlbumController {

    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;

    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;

    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;

    @Resource
    private AlbumInfoService albumInfoService;


    /**
     * 因为没有业务所以直接在controller写
     */
    @PostMapping("/queryCategory")
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "queryCategory")
    @AdminLogin
    public Result<JSONObject> queryCategory() {
        JSONObject jsonObject = new JSONObject();
        //一级分类
        jsonObject.put("category1Id", baseCategory1Mapper.selectList(null));
        jsonObject.put("category2Id", baseCategory2Mapper.selectList(null));
        jsonObject.put("category3Id", baseCategory3Mapper.selectList(null));
//        jsonObject.put("queryCategory",)
        return Result.ok(jsonObject);
    }

    @PostMapping("/importAlbum")
    @OperatorLogAnnotation(operatorType = "1", operatorMethod = "importAlbum")
    @AdminLogin
    public Result<List<AlbumInfoIndex>> importAlbum() {
        return Result.ok(albumInfoService.importAlbum());
    }
}
