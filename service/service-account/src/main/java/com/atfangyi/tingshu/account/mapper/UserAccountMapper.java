package com.atfangyi.tingshu.account.mapper;

import com.atfangyi.tingshu.model.account.UserAccount;
import com.atfangyi.tingshu.vo.account.AccountLockVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;


public interface UserAccountMapper extends BaseMapper<UserAccount> {

    Integer checkAndLock(@Param("accountLockVo") AccountLockVo accountLockVo);

    Integer  recharge(@Param("userId") Long userId,@Param("amount") BigDecimal rechargeAmount);

}
