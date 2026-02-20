package com.atfangyi.tingshu.payment.service.impl;

import cn.hutool.core.lang.Assert;
import com.atfangyi.tingshu.account.AccountFeignClient;
import com.atfangyi.tingshu.model.account.RechargeInfo;
import com.atfangyi.tingshu.payment.config.WxPayV3Config;
import com.atfangyi.tingshu.payment.service.PaymentInfoService;
import com.atfangyi.tingshu.payment.service.WxPayService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private WxPayV3Config wxPayV3Config;

    @Autowired
    private AccountFeignClient accountFeignClient;


    @Override
    public Map<String, String> createJsapi(String paymentType, String orderNo) {
        HashMap<String, String> map = new HashMap<>();
        // 构建service
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(wxPayV3Config.rsaAutoCertificateConfig()).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(1);
        request.setAmount(amount);
        request.setAppid(wxPayV3Config.getAppid());
        request.setMchid(wxPayV3Config.getMerchantId());
        request.setDescription("测试商品");
        request.setNotifyUrl("http://fd7da776.natappfree.cc/api/payment/notify");
        request.setOutTradeNo(orderNo);
        Payer payer = new Payer();
        // 调用下单方法，得到应答
        payer.setOpenid("odo3j4ujPBRopdATZnxKZ3HDOLAc");
        request.setPayer(payer);
        PrepayWithRequestPaymentResponse prepayWithRequestPaymentResponse = service.prepayWithRequestPayment(request);
        // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
        if (prepayWithRequestPaymentResponse != null) {
            map.put("timeStamp", prepayWithRequestPaymentResponse.getTimeStamp());
            map.put("package", prepayWithRequestPaymentResponse.getPackageVal());
            map.put("paySign", prepayWithRequestPaymentResponse.getPaySign());
            map.put("signType", prepayWithRequestPaymentResponse.getSignType());
            map.put("nonceStr", prepayWithRequestPaymentResponse.getNonceStr());
            return map;
        }
        return map;

    }

    @Override
//    @GlobalTransactional(rollbackFor = Exception.class)
    public Map<String, String> Notify(String orderNo) {
        HashMap<String, String> map = new HashMap<>();
        RechargeInfo rechargeInfo = accountFeignClient.getRechargeInfo(orderNo).getData();
        Assert.notNull(rechargeInfo, "充值订单不存在");
        if ("0901".equals(rechargeInfo.getRechargeStatus())) {
            //充值订单未支付 回调接口修改订单状态
            accountFeignClient.UpdateStatus(orderNo);
            accountFeignClient.recharge(orderNo);
        }
        map.put("return_code", "SUCCESS");
        return map;
    }
}
