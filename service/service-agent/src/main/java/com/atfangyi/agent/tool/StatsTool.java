package com.atfangyi.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.atfangyi.agent.util.AiHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 数据统计工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatsTool {

    private final AiHttpClient httpClient;

    @Tool(name = "get_dashboard_stats", description = "获取仪表盘统计数据，包括专辑数、声音数、用户数、浏览量等")
    public String getDashboardStats() {
        try {
            String body = httpClient.postNoBody("/admin/search/queryAlbumDetailCount");
            if (body == null) return "请求失败，请稍后重试";

            JSONObject data = JSON.parseObject(body).getJSONObject("data");
            if (data == null) return "暂无统计数据";

            return String.format("""
                    📊 平台数据概览:

                    有声书专辑: %s 个
                    声音/章节: %s 个
                    注册用户: %s 位
                    总浏览量: %s 次
                    """,
                    data.getLong("albumCount"),
                    data.getLong("trackCount"),
                    data.getLong("userCount"),
                    data.getLong("viewCount")
            );
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", e.getMessage());
            return "获取统计数据失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_today_stats", description = "获取今日数据统计")
    public String getTodayStats() {
        try {
            String body = httpClient.postNoBody("/admin/search/queryAlbumDetailCount");
            if (body == null) return "请求失败，请稍后重试";

            JSONObject data = JSON.parseObject(body).getJSONObject("data");
            if (data == null) return "暂无今日数据";

            return String.format("""
                    📈 平台统计:

                    专辑总数: %s
                    声音总数: %s
                    用户总数: %s
                    浏览总量: %s
                    """,
                    data.getLong("albumCount"),
                    data.getLong("trackCount"),
                    data.getLong("userCount"),
                    data.getLong("viewCount")
            );
        } catch (Exception e) {
            log.error("获取今日数据失败: {}", e.getMessage());
            return "获取今日数据失败: " + e.getMessage();
        }
    }
}
