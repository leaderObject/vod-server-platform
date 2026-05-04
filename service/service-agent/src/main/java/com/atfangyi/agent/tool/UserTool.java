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
 * 用户管理工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserTool {

    private final AiHttpClient httpClient;

    @Tool(name = "get_user_count", description = "获取平台用户总数")
    public String getUserCount() {
        try {
            String body = httpClient.get("/admin/user/internal/user/count");
            if (body == null) return "请求失败，请稍后重试";
            JSONObject json = JSON.parseObject(body);
            return "用户总数: " + json.getLong("data");
        } catch (Exception e) {
            log.error("获取用户总数失败: {}", e.getMessage());
            return "获取用户总数失败: " + e.getMessage();
        }
    }

    @Tool(name = "query_user_list", description = "查询用户列表")
    public String queryUserList(
            @ToolParam(description = "页码，从1开始") int page,
            @ToolParam(description = "每页数量") int size) {
        try {
            String body = httpClient.post("/admin/user/queryAllUserInfo", JSON.toJSONString(new JSONObject() {{
                put("page", page);
                put("size", size);
            }}));

            if (body == null) return "请求失败，请稍后重试";
            JSONObject json = JSON.parseObject(body);
            JSONArray records = json.getJSONArray("data");
            if (records == null || records.isEmpty()) {
                return "暂无用户数据";
            }

            StringBuilder sb = new StringBuilder("用户列表:\n");
            for (int i = 0; i < Math.min(records.size(), 10); i++) {
                JSONObject user = records.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append("昵称: ").append(user.getString("nickName"))
                        .append(" | 手机: ").append(user.getString("phone"))
                        .append(" | VIP: ").append(user.getBoolean("vipStatus") ? "是" : "否")
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("查询用户列表失败: {}", e.getMessage());
            return "查询用户列表失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_user_detail", description = "获取用户详细信息")
    public String getUserDetail(
            @ToolParam(description = "用户ID") Long userId) {
        try {
            String body = httpClient.post("/admin/user/queryUserInfoById/" + userId, "{}");
            if (body == null) return "请求失败，请稍后重试";

            JSONObject user = JSON.parseObject(body).getJSONObject("data");
            if (user == null) return "未找到用户信息";

            return String.format("""
                    用户详情:
                    用户ID: %s
                    昵称: %s
                    手机号: %s
                    VIP状态: %s
                    注册时间: %s
                    余额: %s
                    """,
                    user.getLong("id"),
                    user.getString("nickName"),
                    user.getString("phone"),
                    user.getBoolean("vipStatus") ? "VIP会员" : "普通用户",
                    user.getString("createTime"),
                    user.getBigDecimal("balance")
            );
        } catch (Exception e) {
            log.error("获取用户详情失败: {}", e.getMessage());
            return "获取用户详情失败: " + e.getMessage();
        }
    }
}
