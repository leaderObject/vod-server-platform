package com.atfangyi.tingshu.account.impl;


import com.atfangyi.tingshu.account.AccountFeignClient;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.account.RechargeInfo;
import com.atfangyi.tingshu.vo.account.AccountLockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class AccountDegradeFeignClient implements AccountFeignClient {

    @Override
    public Result<BigDecimal> getAvailableAmount() {
        log.error("[用户账户远程调用失败执行熔断降级{}]", "getAvailableAmount");
        return null;
    }

    @Override
    public Result<AccountLockVo> checkAndLock(AccountLockVo accountLockVo) {
        log.error("[用户账户远程调用失败执行熔断降级{}]", "checkAndLock");
        return null;
    }

    @Override
    public Result<RechargeInfo> getRechargeInfo(String orderNo) {
        log.error("[用户账户远程调用失败执行熔断降级{}]", "checkAndLock");
        return null;
    }

    @Override
    public void recharge(String orderNo) {
        log.error("[用户账户远程调用失败执行熔断降级{}]", "recharge");

    }

    @Override
    public void UpdateStatus(String orderNo) {
        log.error("[用户账户远程调用失败执行熔断降级{}]", "UpdateStatus");

    }
}
