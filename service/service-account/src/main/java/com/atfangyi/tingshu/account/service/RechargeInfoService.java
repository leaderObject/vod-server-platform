package com.atfangyi.tingshu.account.service;

import com.atfangyi.tingshu.model.account.RechargeInfo;
import com.atfangyi.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface RechargeInfoService extends IService<RechargeInfo> {

    Map<String, String> submitRecharge(RechargeInfoVo rechargeInfoVo);

    void UpdateStatus(String orderNo);

}
