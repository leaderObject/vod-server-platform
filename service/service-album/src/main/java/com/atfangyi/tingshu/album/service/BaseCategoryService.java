package com.atfangyi.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.model.album.BaseAttribute;
import com.atfangyi.tingshu.model.album.BaseCategory1;
import com.atfangyi.tingshu.model.album.BaseCategory3;
import com.atfangyi.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.List;

public interface BaseCategoryService extends IService<BaseCategory1> {


    ArrayList<JSONObject> getBaseCategoryList();

    List<BaseAttribute> categoryFindAttribute(Long category1Id);

    BaseCategoryView getCategoryView(Long category3Id);

    List<BaseCategory3> findTopBaseCategory3(Long category1Id);


    JSONObject getBaseCategoryListBycategory1Id(Long category1Id);
}
