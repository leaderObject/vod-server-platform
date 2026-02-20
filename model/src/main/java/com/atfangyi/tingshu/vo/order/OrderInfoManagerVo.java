package com.atfangyi.tingshu.vo.order;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderInfoManagerVo {
    private Long id;
    private Long userId;
    private String orderTitle;
    private String orderNo;
    private String orderStatus;
    private String payWay;
    private String itemName;
    private String itemUrl;
    private BigDecimal itemPrice;
    private BigDecimal orderAmount;
    private BigDecimal derateAmount;



}
