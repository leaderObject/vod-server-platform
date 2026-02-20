package com.atfangyi.tingshu.order.client.impl;


import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.order.client.OrderFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OrderDegradeFeignClient implements FallbackFactory<OrderFeignClient> {

    @Override
    public OrderFeignClient create(Throwable cause) {
        return (ItemType, OrderStatus) -> {
            log.error("远程调用出现异常{}", cause.getMessage());
            return null;
        };
    }
}
