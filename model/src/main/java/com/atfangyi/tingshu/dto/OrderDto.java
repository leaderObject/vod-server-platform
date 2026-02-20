package com.atfangyi.tingshu.dto;

import lombok.Data;

@Data
public class OrderDto {

    private String orderNo;

    private Long userId;

    private String orderStatus;

    private String payWay;

}
