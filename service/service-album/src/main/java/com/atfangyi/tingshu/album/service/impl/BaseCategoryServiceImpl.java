package com.atfangyi.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.mapper.*;
import com.atfangyi.tingshu.album.service.BaseCategoryService;
import com.atfangyi.tingshu.common.annotation.Cache;
import com.atfangyi.tingshu.common.constant.ServiceConstant;
import com.atfangyi.tingshu.model.album.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.v3.core.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"all"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private BaseAttributeMapper baseAttributeMapper;

    @Autowired
    private BaseAttributeValueMapper baseAttributeValueMapper;


    @Override
    public ArrayList<JSONObject> getBaseCategoryList() {
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        if (!CollectionUtils.isEmpty(baseCategoryViews)) {
            Map<Long, List<BaseCategoryView>> baseCategory1 = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
            for (Map.Entry<Long, List<BaseCategoryView>> longListEntry : baseCategory1.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                Long key = longListEntry.getKey();
                String category1Name = longListEntry.getValue().get(0).getCategory1Name();
                jsonObject.put(ServiceConstant.CATEGORY_ID, key);
                jsonObject.put(ServiceConstant.CATEGORY_NAME, category1Name);
                Map<Long, List<BaseCategoryView>> baseCategory2 = longListEntry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
                ArrayList<JSONObject> baseCategory2JsonObjects = new ArrayList<>();
                for (Map.Entry<Long, List<BaseCategoryView>> listEntry : baseCategory2.entrySet()) {
                    JSONObject baseCategory2JsonObject = new JSONObject();
                    Long baseCategory2Id = listEntry.getKey();
                    String baseCategory2Name = listEntry.getValue().get(0).getCategory2Name();
                    baseCategory2JsonObject.put(ServiceConstant.CATEGORY_ID, baseCategory2Id);
                    baseCategory2JsonObject.put(ServiceConstant.CATEGORY_NAME, baseCategory2Name);
                    baseCategory2JsonObject.put(ServiceConstant.CATEGORY_CHILD,
                            listEntry.getValue().stream().map(baseCategoryView -> {
                                JSONObject baseCategory3JsonObject = new JSONObject();
                                baseCategory3JsonObject.put(ServiceConstant.CATEGORY_ID, baseCategoryView.getCategory3Id());
                                baseCategory3JsonObject.put(ServiceConstant.CATEGORY_NAME, baseCategoryView.getCategory3Name());
                                return baseCategory3JsonObject;
                            }).collect(Collectors.toList()));
                    jsonObject.put(ServiceConstant.CATEGORY_CHILD, baseCategory2JsonObjects);
                    baseCategory2JsonObjects.add(baseCategory2JsonObject);
                }
                jsonObject.put(ServiceConstant.CATEGORY_CHILD, baseCategory2JsonObjects);
                jsonObjects.add(jsonObject);
            }
        }

        return jsonObjects;
    }

    @Override
    public List<BaseAttribute> categoryFindAttribute(Long category1Id) {
        List<BaseAttribute> baseAttributes =
                baseAttributeMapper.selectList(Wrappers.lambdaQuery(BaseAttribute.class).eq(BaseAttribute::getCategory1Id, category1Id));
        baseAttributes.stream().forEach(baseAttribute -> {
            baseAttribute.setAttributeValueList(baseAttributeValueMapper.selectList(Wrappers.lambdaQuery(BaseAttributeValue.class)
                    .eq(BaseAttributeValue::getAttributeId, baseAttribute.getId())));
        });
        return baseAttributes;
    }

    @Override
    @Cache(prefix = "CategoryView")
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    public List<BaseCategory3> findTopBaseCategory3(Long category1Id) {
        List<Long> collect = baseCategory2Mapper.selectList(Wrappers.lambdaQuery(BaseCategory2.class).eq(BaseCategory2::getCategory1Id, category1Id))
                .stream().map(baseCategory2 -> baseCategory2.getId()).collect(Collectors.toList());
        LambdaQueryWrapper<BaseCategory3> baseCategory3LambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategory3LambdaQueryWrapper.eq(BaseCategory3::getIsTop, 1);
        baseCategory3LambdaQueryWrapper.in(BaseCategory3::getCategory2Id, collect);
        baseCategory3LambdaQueryWrapper.orderByDesc(BaseCategory3::getOrderNum);
        baseCategory3LambdaQueryWrapper.last("limit 7");
        return baseCategory3Mapper.selectList(baseCategory3LambdaQueryWrapper);
    }

    @Override
    public JSONObject getBaseCategoryListBycategory1Id(Long category1Id) {
        JSONObject jsonObject = new JSONObject();
        List<BaseCategoryView> baseCategoryViews =
                baseCategoryViewMapper.selectList(Wrappers.lambdaQuery(BaseCategoryView.class).eq(BaseCategoryView::getCategory1Id, category1Id));
        if (!CollectionUtils.isEmpty(baseCategoryViews)) {
            Map<Long, List<BaseCategoryView>> collect = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            ArrayList<JSONObject> jsonObjects = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> entry : collect.entrySet()) {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("categoryId", entry.getKey());
                jsonObject1.put("categoryName", entry.getValue().get(0).getCategory2Name());
                ArrayList<JSONObject> jsonObjects1 = new ArrayList<>();
                entry.getValue().forEach(baseCategoryView -> {
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put("categoryId", baseCategoryView.getCategory3Id());
                    jsonObject2.put("categoryName", baseCategoryView.getCategory3Name());
                    jsonObjects1.add(jsonObject2);
                });
                jsonObject1.put("categoryChild", jsonObjects1);
                jsonObjects.add(jsonObject1);

            }
            jsonObject.put("categoryId", baseCategoryViews.get(0).getCategory1Id());
            jsonObject.put("categoryName", baseCategoryViews.get(0).getCategory1Name());
            jsonObject.put("categoryChild", jsonObjects);
        }
        return jsonObject;


    }
}
