package com.atfangyi.tingshu.account.service.impl;

import cn.hutool.core.util.IdUtil;
import com.atfangyi.tingshu.account.mapper.RechargeInfoMapper;
import com.atfangyi.tingshu.account.service.RechargeInfoService;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.model.account.RechargeInfo;
import com.atfangyi.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings({"all"})
public class RechargeInfoServiceImpl extends ServiceImpl<RechargeInfoMapper, RechargeInfo> implements RechargeInfoService {

    @Autowired
    private RechargeInfoMapper rechargeInfoMapper;

    @Override
    public Map<String, String> submitRecharge(RechargeInfoVo rechargeInfoVo) {
        HashMap<String, String> map = new HashMap<>();
        //获取充值额度
        BigDecimal amount = rechargeInfoVo.getAmount();
        //获取支付方式
        String payWay = rechargeInfoVo.getPayWay();
        //生成订单
        RechargeInfo rechargeInfo = new RechargeInfo();
        rechargeInfo.setUserId(AuthContextHolder.getUserId());
        rechargeInfo.setPayWay(payWay);
        rechargeInfo.setRechargeAmount(amount);
        String orderNo = "CZ" + IdUtil.getSnowflakeNextIdStr();
        rechargeInfo.setOrderNo(orderNo);
        rechargeInfo.setRechargeStatus("0901");
        rechargeInfoMapper.insert(rechargeInfo);
        map.put("orderNo", orderNo);
        return map;
    }

    @Override
    @Transactional
    public void UpdateStatus(String orderNo) {
        RechargeInfo rechargeInfo = rechargeInfoMapper.selectOne(Wrappers.lambdaQuery(RechargeInfo.class).eq(RechargeInfo::getOrderNo, orderNo));
        rechargeInfo.setRechargeStatus("0902");
        if (rechargeInfoMapper.updateById(rechargeInfo) < 1) throw new GuiguException(ResultCodeEnum.FAIL);
    }
}
