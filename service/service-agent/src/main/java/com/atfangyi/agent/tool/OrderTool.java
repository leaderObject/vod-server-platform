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
 * 订单管理工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTool {

    private final AiHttpClient httpClient;

    @Tool(name = "query_order_list", description = "查询订单列表")
    public String queryOrderList(
            @ToolParam(description = "页码，从1开始") int page,
            @ToolParam(description = "每页数量") int size,
            @ToolParam(description = "订单状态: 0901待支付, 0902已支付, 0903已取消, 0904已退款") String orderStatus) {
        try {
            JSONObject params = new JSONObject();
            if (orderStatus != null && !orderStatus.isEmpty()) {
                params.put("orderStatus", orderStatus);
            }
            String body = httpClient.post("/admin/order/orderInfo/queryOrderInfo/" + page + "/" + size,
                    params.isEmpty() ? "{}" : JSON.toJSONString(params));

            return formatOrderList(body);
        } catch (Exception e) {
            log.error("查询订单列表失败: {}", e.getMessage());
            return "查询订单列表失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_order_detail", description = "获取订单详细信息")
    public String getOrderDetail(
            @ToolParam(description = "订单号") String orderNo) {
        try {
            String body = httpClient.get("/admin/order/orderInfo/queryOrderInfoById/" + orderNo);
            if (body == null) return "请求失败，请稍后重试";

            JSONObject order = JSON.parseObject(body).getJSONObject("data");
            if (order == null) return "未找到订单信息";

            return String.format("""
                    订单详情:
                    订单号: %s
                    订单金额: %s元
                    订单状态: %s
                    支付方式: %s
                    商品类型: %s
                    创建时间: %s
                    """,
                    order.getString("orderNo"),
                    order.getBigDecimal("totalAmount"),
                    getOrderStatusText(order.getString("orderStatus")),
                    order.getString("payWay"),
                    order.getString("itemType"),
                    order.getString("createTime")
            );
        } catch (Exception e) {
            log.error("获取订单详情失败: {}", e.getMessage());
            return "获取订单详情失败: " + e.getMessage();
        }
    }

    @Tool(name = "get_pending_orders", description = "获取待处理订单（未支付和退款申请）")
    public String getPendingOrders(
            @ToolParam(description = "页码") int page,
            @ToolParam(description = "每页数量") int size) {
        try {
            JSONObject params = new JSONObject();
            params.put("orderStatus", "0901"); // 待支付
            String body = httpClient.post("/admin/order/orderInfo/queryOrderInfo/" + page + "/" + size,
                    JSON.toJSONString(params));
            return formatOrderList(body);
        } catch (Exception e) {
            log.error("查询待处理订单失败: {}", e.getMessage());
            return "查询待处理订单失败: " + e.getMessage();
        }
    }

    private String formatOrderList(String body) {
        if (body == null) return "请求失败，请稍后重试";
        try {
            JSONObject json = JSON.parseObject(body);
            JSONArray records = json.getJSONArray("data");
            if (records == null || records.isEmpty()) {
                return "暂无订单数据";
            }

            StringBuilder sb = new StringBuilder("订单列表:\n");
            for (int i = 0; i < Math.min(records.size(), 10); i++) {
                JSONObject order = records.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append("订单号: ").append(order.getString("orderNo"))
                        .append(" | 金额: ").append(order.getBigDecimal("totalAmount")).append("元")
                        .append(" | 状态: ").append(getOrderStatusText(order.getString("orderStatus")))
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return body;
        }
    }

    private String getOrderStatusText(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case "0901" -> "待支付";
            case "0902" -> "已支付";
            case "0903" -> "已取消";
            case "0904" -> "已退款";
            default -> status;
        };
    }
}
