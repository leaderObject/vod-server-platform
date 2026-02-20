package com.atfangyi.tingshu.user.strategy.Impl;

/*
 * @Author:  方毅
 * @date:  2025/11/5 21:38
 */

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.user.mapper.UserInfoMapper;
import com.atfangyi.tingshu.user.mapper.UserVipServiceMapper;
import com.atfangyi.tingshu.user.strategy.UserPaidStrategy;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component(SystemConstant.ORDER_ITEM_TYPE_VIP)
public class UserPaidVip implements UserPaidStrategy {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserVipServiceMapper userVipServiceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserPaid(UserPaidRecordVo userPaidRecordVo) {
        UserInfo userInfo = userInfoMapper.selectById(userPaidRecordVo.getUserId());
        Assert.notNull(userInfo, "数据出现异常userInfo");
        //获取用户开通会员的期数
        Long month = 0L;
        if (CollectionUtil.isNotEmpty(userPaidRecordVo.getItemIdList()))
            month = userPaidRecordVo.getItemIdList().get(0);
        UserVipService userVipService = new UserVipService();
        userVipService.setUserId(userPaidRecordVo.getUserId());
        userVipService.setOrderNo(userPaidRecordVo.getOrderNo());
        userVipService.setStartTime(DateTime.now().toDate());
        userVipService.setExpireTime(DateTime.now().plus(Math.toIntExact(month)).toDate());

        if (userVipServiceMapper.insert(userVipService) < 1) throw new GuiguException(ResultCodeEnum.FAIL);


    }
}
