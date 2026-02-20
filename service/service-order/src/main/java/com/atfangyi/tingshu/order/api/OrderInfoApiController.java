package com.atfangyi.tingshu.order.api;

import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.order.OrderInfo;
import com.atfangyi.tingshu.order.service.OrderInfoService;
import com.atfangyi.tingshu.vo.order.OrderInfoVo;
import com.atfangyi.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "订单管理")
@RestController
@RequestMapping("api/order")
@SuppressWarnings({"all"})
public class OrderInfoApiController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Operation(summary = "订单确认")
    @PostMapping("/orderInfo/trade")
    @Login(required = true)
    public Result<OrderInfoVo> orderInfoTrade(@RequestBody TradeVo tradeVo) {
        return Result.ok(orderInfoService.orderInfoService(tradeVo));
    }

    @Operation(summary = "提交订单")
    @PostMapping("/orderInfo/submitOrder")
    @Login
    public Result<HashMap<String, Object>> submitOrder(@RequestBody OrderInfoVo orderInfoVo) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("orderNo", orderInfoService.submitOrder(orderInfoVo));
        return Result.ok(map);
    }

    @Operation(summary = "根据订单号获取订单相关信息")
    @GetMapping("/orderInfo/getOrderInfo/{orderNo}")
    @Login
    public Result<Map<String, Object>> getOrderInfo(@PathVariable String orderNo) {
        return Result.ok(orderInfoService.getOrderInfo(orderNo));
    }

    @Operation(summary = "分页查询用户订单")
    @GetMapping("/orderInfo/findUserPage/{page}/{size}")
    @Login(required = true)
    public Result<Page<OrderInfo>> findUserPage(@PathVariable Long page, @PathVariable Long size) {
        return Result.ok(orderInfoService.findUserPage(page,size));
    }
}

