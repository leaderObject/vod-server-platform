package com.atfangyi.agent.tool;

import cn.hutool.http.HttpUtil;
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
 * 专辑搜索和排行榜工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchTool {

    private final AiHttpClient httpClient;

    @Tool(name = "search_albums", description = "搜索专辑，根据关键词和分类搜索有声书专辑")
    public String searchAlbums(
            @ToolParam(description = "搜索关键词") String keyword,
            @ToolParam(description = "一级分类ID，可为空") String category1Id,
            @ToolParam(description = "页码，从1开始") int page,
            @ToolParam(description = "每页数量") int size) {
        try {
            String body = httpClient.post("/api/search/albumInfo", JSON.toJSONString(new JSONObject() {{
                put("keyword", keyword);
                put("category1Id", category1Id);
                put("page", page);
                put("size", size);
            }}));
            return formatAlbumList(body);
        } catch (Exception e) {
            log.error("搜索专辑失败: {}", e.getMessage());
            return "搜索专辑失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_album_ranking", description = "获取专辑排行榜")
    public String getAlbumRanking(
            @ToolParam(description = "一级分类ID") String category1Id,
            @ToolParam(description = "排行维度: playCount播放量, subscribeCount订阅量, buyCount购买量") String dimension,
            @ToolParam(description = "返回数量") int limit) {
        try {
            String body = httpClient.get("/api/search/albumInfo/findRankingList/" + category1Id + "/" + dimension);
            return formatAlbumList(body);
        } catch (Exception e) {
            log.error("获取排行榜失败: {}", e.getMessage());
            return "获取排行榜失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_album_detail", description = "获取专辑详情")
    public String getAlbumDetail(
            @ToolParam(description = "专辑ID") String albumId) {
        try {
            String body = httpClient.get("/api/search/albumInfo/" + albumId);
            return formatAlbumDetail(body);
        } catch (Exception e) {
            log.error("获取专辑详情失败: {}", e.getMessage());
            return "获取专辑详情失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_hot_albums", description = "获取热门专辑推荐")
    public String getHotAlbums(
            @ToolParam(description = "一级分类ID，为空则返回全部分类热门") String category1Id,
            @ToolParam(description = "返回数量") int limit) {
        try {
            String path = category1Id != null && !category1Id.isEmpty()
                    ? "/api/search/albumInfo/channel/" + category1Id
                    : "/api/search/albumInfo/channel/1";
            String body = httpClient.get(path);
            return formatAlbumList(body);
        } catch (Exception e) {
            log.error("获取热门专辑失败: {}", e.getMessage());
            return "获取热门专辑失败: " + e.getMessage();
        }
    }

    private String formatAlbumList(String body) {
        if (body == null) return "请求失败，请稍后重试";
        try {
            JSONObject json = JSON.parseObject(body);
            JSONArray records = json.getJSONArray("data");
            if (records == null || records.isEmpty()) {
                return "暂无数据";
            }

            StringBuilder sb = new StringBuilder("专辑列表:\n");
            for (int i = 0; i < Math.min(records.size(), 10); i++) {
                JSONObject album = records.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append("《").append(album.getString("albumTitle")).append("》")
                        .append(" - 播放量: ").append(album.getLong("playCount") != null ? album.getLong("playCount") : 0)
                        .append(" - 订阅量: ").append(album.getLong("subscribeCount") != null ? album.getLong("subscribeCount") : 0)
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return body;
        }
    }

    private String formatAlbumDetail(String body) {
        if (body == null) return "请求失败，请稍后重试";
        try {
            JSONObject album = JSON.parseObject(body).getJSONObject("data");
            if (album == null) return "未找到专辑信息";

            return String.format("""
                    专辑详情:
                    名称: %s
                    作者: %s
                    简介: %s
                    标签: %s
                    播放量: %s
                    订阅量: %s
                    评分: %s
                    """,
                    album.getString("albumTitle"),
                    album.getString("author"),
                    album.getString("albumIntro"),
                    album.getString("albumTags"),
                    album.getLong("playCount"),
                    album.getLong("subscribeCount"),
                    album.getDouble("score")
            );
        } catch (Exception e) {
            return body;
        }
    }
}
