package com.atfangyi.tingshu.order.service.impl;

/*
 * @Author:  方毅
 * @date:  2025/11/5 12:41
 */

import com.atfangyi.tingshu.model.order.OrderDetail;
import com.atfangyi.tingshu.order.mapper.OrderDetailMapper;
import com.atfangyi.tingshu.order.service.OrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl  extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
