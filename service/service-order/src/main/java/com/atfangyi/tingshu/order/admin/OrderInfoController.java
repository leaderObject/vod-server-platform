package com.atfangyi.tingshu.order.admin;

import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.OrderDto;
import com.atfangyi.tingshu.model.order.OrderInfo;
import com.atfangyi.tingshu.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/admin/order/orderInfo")
@SuppressWarnings({"all"})
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;
    //解决了订单的一个bug
    @PostMapping("/queryOrderInfo/{current}/{size}")
    @Operation(summary = "查询订单信息")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4",operatorMethod = "queryOrderInfo")
    public Result queryOrderInfo(@RequestBody OrderDto orderDto, @PathVariable Long current, @PathVariable Long size) {
        return Result.ok(orderInfoService.queryOrderInfo(current, size, orderDto));
    }

    ///order/orderInfo/queryOrderInfoById/{id}
    @GetMapping("/queryOrderInfoById/{id}")
    @Operation(summary = "根据Id查询订单详细信息")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4",operatorMethod = "queryOrderInfoById")
    public Result queryOrderInfoById(@PathVariable Long id) {
        return Result.ok(orderInfoService.queryOrderInfoById(id));
    }

    @PostMapping("/queryOrderInfoByItemType/{ItemType}/{OrderStatus}")
    @Operation(summary = "查询不同不同类型订单项目的Id集合")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4",operatorMethod = "queryOrderInfoByItemType")
    public Result<List<Long>> queryOrderInfoByItemType(@PathVariable String ItemType, @PathVariable String OrderStatus) {
        return Result.ok(orderInfoService.queryOrderInfoByItemType(ItemType, OrderStatus));
    }


}

