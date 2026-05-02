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
        Long userId = userPaidRecordVo.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Assert.notNull(userInfo, "数据出现异常userInfo");

        Long month = 0L;
        if (CollectionUtil.isNotEmpty(userPaidRecordVo.getItemIdList()))
            month = userPaidRecordVo.getItemIdList().get(0);

        DateTime now = DateTime.now();
        // 已是会员且未过期：在原到期时间基础上累加
        // 非会员或已过期：从当前时间开始计算
        boolean isVipActive = userInfo.getIsVip() != null && userInfo.getIsVip() == 1
                && userInfo.getVipExpireTime() != null && userInfo.getVipExpireTime().after(now.toDate());
        DateTime baseTime = isVipActive ? new DateTime(userInfo.getVipExpireTime()) : now;
        DateTime expireTime = baseTime.plusMonths(Math.toIntExact(month));
        DateTime startTime = isVipActive ? new DateTime(userInfo.getVipExpireTime()) : now;

        UserVipService userVipService = new UserVipService();
        userVipService.setUserId(userId);
        userVipService.setOrderNo(userPaidRecordVo.getOrderNo());
        userVipService.setStartTime(startTime.toDate());
        userVipService.setExpireTime(expireTime.toDate());

        if (userVipServiceMapper.insert(userVipService) < 1) throw new GuiguException(ResultCodeEnum.FAIL);

        userInfo.setIsVip(1);
        userInfo.setVipExpireTime(expireTime.toDate());
        userInfoMapper.updateById(userInfo);
    }
}
