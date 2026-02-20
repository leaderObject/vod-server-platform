package com.atfangyi.tingshu.account.service;

import com.atfangyi.tingshu.model.account.UserAccount;
import com.atfangyi.tingshu.vo.account.AccountLockVo;
import com.atfangyi.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface UserAccountService extends IService<UserAccount> {


    void saveUserAccount(String value);


    BigDecimal getAvailableAmount(Long userId);


    AccountLockVo checkAndLock(AccountLockVo accountLockVo);


    void recharge(String orderNo);
}
