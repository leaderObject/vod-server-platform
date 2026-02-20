package com.atfangyi.tingshu.account.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.atfangyi.tingshu.account.mapper.RechargeInfoMapper;
import com.atfangyi.tingshu.account.mapper.UserAccountDetailMapper;
import com.atfangyi.tingshu.account.mapper.UserAccountMapper;
import com.atfangyi.tingshu.account.service.UserAccountService;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.model.account.RechargeInfo;
import com.atfangyi.tingshu.model.account.UserAccount;
import com.atfangyi.tingshu.model.account.UserAccountDetail;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.vo.account.AccountLockVo;
import com.atfangyi.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencentcloudapi.sqlserver.v20180328.models.AccountDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserAccountDetailMapper userAccountDetailMapper;

    @Autowired
    private RechargeInfoMapper rechargeInfoMapper;



    @Override
    public void saveUserAccount(String value) {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(Long.parseLong(value));
        userAccount.setAvailableAmount(new BigDecimal(100));
        userAccount.setTotalAmount(new BigDecimal(100));
        userAccount.setTotalIncomeAmount(new BigDecimal(100));
        userAccountMapper.insert(userAccount);
    }

    @Override
    public BigDecimal getAvailableAmount(Long userId) {
        UserAccount userAccount = userAccountMapper.selectOne(Wrappers.lambdaQuery(UserAccount.class).eq(UserAccount::getUserId, userId));
        if (userAccount == null) throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        return userAccount.getAvailableAmount();

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public AccountLockVo checkAndLock(AccountLockVo accountLockVo) {
        Integer result = userAccountMapper.checkAndLock(accountLockVo);
        if (result < 1) throw new GuiguException(ResultCodeEnum.ACCOUNT_MINUSLOCK_ERROR);
        UserAccountDetail userAccountDetail = new UserAccountDetail();
        userAccountDetail.setUserId(accountLockVo.getUserId());
        userAccountDetail.setTitle(accountLockVo.getContent());
        userAccountDetail.setTradeType(SystemConstant.ACCOUNT_TRADE_TYPE_MINUS);
        userAccountDetail.setOrderNo(accountLockVo.getOrderNo());
        userAccountDetail.setAmount(accountLockVo.getAmount());
        if (userAccountDetailMapper.insert(userAccountDetail) < 1)
            throw new GuiguException(ResultCodeEnum.ACCOUNT_MINUSLOCK_ERROR);
        return accountLockVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(String orderNo) {
        RechargeInfo rechargeInfo = rechargeInfoMapper.selectOne(Wrappers.lambdaQuery(RechargeInfo.class).eq(RechargeInfo::getOrderNo, orderNo));
        Assert.notNull(rechargeInfo, "充值订单不存在");
        Integer recharge = userAccountMapper.recharge(AuthContextHolder.getUserId(), rechargeInfo.getRechargeAmount());
        if (recharge <1) throw  new GuiguException(ResultCodeEnum.FAIL);
        UserAccountDetail userAccountDetail = new UserAccountDetail();
        userAccountDetail.setUserId(AuthContextHolder.getUserId());
        userAccountDetail.setTitle("用户"+AuthContextHolder.getUserId()+"账户充值"+rechargeInfo.getRechargeAmount());
        userAccountDetail.setTradeType(SystemConstant.ACCOUNT_TRADE_TYPE_DEPOSIT);
        userAccountDetail.setOrderNo(orderNo);
        userAccountDetail.setAmount(rechargeInfo.getRechargeAmount());
        if (userAccountDetailMapper.insert(userAccountDetail)<1)  throw new GuiguException(ResultCodeEnum.FAIL);
    }

}
