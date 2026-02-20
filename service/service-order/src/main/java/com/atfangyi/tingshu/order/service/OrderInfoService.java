package com.atfangyi.tingshu.order.service;

import com.atfangyi.tingshu.dto.OrderDto;
import com.atfangyi.tingshu.model.order.OrderInfo;
import com.atfangyi.tingshu.vo.order.OrderInfoManagerVo;
import com.atfangyi.tingshu.vo.order.OrderInfoVo;
import com.atfangyi.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface OrderInfoService extends IService<OrderInfo> {


    OrderInfoVo orderInfoService(TradeVo tradeVo);


    String submitOrder(OrderInfoVo orderInfoVo);

    Map<String, Object> getOrderInfo(String orderNo);

    Page<OrderInfo> findUserPage(Long page, Long size);


    IPage<OrderInfoManagerVo> queryOrderInfo(Long current, Long size, OrderDto orderDto);

    List<OrderInfoManagerVo> queryOrderInfoById(Long id);

    List<Long> queryOrderInfoByItemType(String itemType, String orderStatus);


}
