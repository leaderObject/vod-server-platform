package com.atfangyi.tingshu.order.client;

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * <p>
 * 订单模块远程调用API接口
 * </p>
 *
 * @author atguigu
 */
@FeignClient(value = "service-order", path = "/admin/order/orderInfo", fallbackFactory = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    @PostMapping("/queryOrderInfoByItemType/{ItemType}/{OrderStatus}")
     Result<List<Long>> queryOrderInfoByItemType(@PathVariable String ItemType, @PathVariable String OrderStatus);


}
