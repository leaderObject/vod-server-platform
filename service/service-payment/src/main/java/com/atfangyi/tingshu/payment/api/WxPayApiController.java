package com.atfangyi.tingshu.payment.api;

import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.payment.service.WxPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "微信支付接口")
@RestController
@RequestMapping("api/payment")
@Slf4j
public class WxPayApiController {

    @Autowired
    private WxPayService wxPayService;


    @Operation(summary = "微信下单")
    @PostMapping("/wxPay/createJsapi/{paymentType}/{orderNo}")
    @Login
    public Result<Map<String, String>> createJsapi(@PathVariable String paymentType, @PathVariable String orderNo) {
        return Result.ok(wxPayService.createJsapi(paymentType, orderNo));
    }

    @Operation(summary = "模拟微支付成功回调接口")
    @GetMapping("/notify")
    public Result<Map<String, String>> Notify(HttpServletRequest httpServletRequest) {
        return Result.ok(wxPayService.Notify(httpServletRequest.getParameter("orderNo")));
    }

}
