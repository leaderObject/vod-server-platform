package com.atfangyi.tingshu.user.strategy.Impl;

/*
 * @Author:  方毅
 * @date:  2025/11/5 21:37
 */

import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.user.strategy.UserPaidStrategy;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import org.springframework.stereotype.Component;

@Component(SystemConstant.ORDER_ITEM_TYPE_TRACK)
public class UserPaidTrack  implements UserPaidStrategy {
    @Override
    public void saveUserPaid(UserPaidRecordVo userPaidRecordVo) {

    }
}
