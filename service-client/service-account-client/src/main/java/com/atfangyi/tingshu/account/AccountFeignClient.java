package com.atfangyi.tingshu.account;

import com.atfangyi.tingshu.account.impl.AccountDegradeFeignClient;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.model.account.RechargeInfo;
import com.atfangyi.tingshu.vo.account.AccountLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

/**
 * <p>
 * 账号模块远程调用API接口
 * </p>
 *
 * @author atguigu
 */
@FeignClient(value = "service-account", path = "/api/account", fallback = AccountDegradeFeignClient.class)
public interface AccountFeignClient {

    @GetMapping("/userAccount/getAvailableAmount")
    Result<BigDecimal> getAvailableAmount();


    @PostMapping("/userAccount/checkAndLock")
    Result<AccountLockVo> checkAndLock(@RequestBody AccountLockVo accountLockVo);


    @GetMapping("/rechargeInfo/getRechargeInfo/{orderNo}")
    Result<RechargeInfo> getRechargeInfo(@PathVariable String orderNo);


    @PostMapping("/userAccount/recharge/{orderNo}")
    void recharge(@PathVariable String orderNo);


    @GetMapping("/userAccount/UpdateStatus/{orderNo}")
    void UpdateStatus(@PathVariable String orderNo);


}
