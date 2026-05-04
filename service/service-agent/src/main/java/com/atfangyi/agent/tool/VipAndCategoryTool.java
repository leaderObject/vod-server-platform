package com.atfangyi.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.atfangyi.agent.util.AiHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * VIP和分类管理工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VipAndCategoryTool {

    private final AiHttpClient httpClient;

    @Tool(name = "get_vip_config_list", description = "获取VIP会员套餐列表")
    public String getVipConfigList() {
        try {
            String body = httpClient.get("/api/user/vipServiceConfig/findAll");
            if (body == null) return "请求失败，请稍后重试";

            JSONArray list = JSON.parseObject(body).getJSONArray("data");
            if (list == null || list.isEmpty()) {
                return "暂无VIP套餐数据";
            }

            StringBuilder sb = new StringBuilder("VIP会员套餐:\n");
            for (int i = 0; i < list.size(); i++) {
                JSONObject vip = list.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append(vip.getString("serviceName"))
                        .append(" - 价格: ").append(vip.getBigDecimal("price")).append("元")
                        .append(" | 有效期: ").append(vip.getInteger("validDays")).append("天")
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("获取VIP配置失败: {}", e.getMessage());
            return "获取VIP配置失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_category_list", description = "获取平台分类列表")
    public String getCategoryList() {
        try {
            String body = httpClient.get("/api/album/category/getBaseCategoryList");
            return formatCategoryList(body);
        } catch (Exception e) {
            log.error("获取分类列表失败: {}", e.getMessage());
            return "获取分类列表失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_category_albums", description = "获取某个分类下的专辑列表")
    public String getCategoryAlbums(
            @ToolParam(description = "一级分类ID") String category1Id,
            @ToolParam(description = "返回数量") int limit) {
        try {
            String body = httpClient.get("/api/search/albumInfo/channel/" + category1Id);
            if (body == null) return "请求失败，请稍后重试";

            JSONArray albums = JSON.parseObject(body).getJSONArray("data");
            if (albums == null || albums.isEmpty()) {
                return "该分类下暂无专辑";
            }

            StringBuilder sb = new StringBuilder("分类下的专辑:\n");
            for (int i = 0; i < Math.min(albums.size(), limit); i++) {
                JSONObject album = albums.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append("《").append(album.getString("albumTitle")).append("》")
                        .append(" - 播放量: ").append(album.getLong("playCount"))
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("获取分类专辑失败: {}", e.getMessage());
            return "获取分类专辑失败: " + e.getMessage();
        }
    }

    private String formatCategoryList(String body) {
        if (body == null) return "请求失败，请稍后重试";
        try {
            JSONObject json = JSON.parseObject(body);
            JSONArray category1 = json.getJSONArray("category1");
            if (category1 == null || category1.isEmpty()) {
                return "暂无分类数据";
            }

            StringBuilder sb = new StringBuilder("平台分类:\n");
            for (int i = 0; i < category1.size(); i++) {
                JSONObject cat = category1.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append(cat.getString("name"))
                        .append(" (ID: ").append(cat.getString("id")).append(")")
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return body;
        }
    }
}
