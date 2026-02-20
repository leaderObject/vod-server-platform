package com.atfangyi.tingshu.payment.service;

import java.util.Map;

public interface WxPayService {

    Map<String, String> createJsapi(String paymentType, String orderNo);

    Map<String, String> Notify(String orderNo);

}
