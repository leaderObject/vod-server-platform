package com.atfangyi.tingshu.user.strategy;

import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;

/*
 * @Author:  方毅
 * @date:  2025/11/5 21:33
 */
public interface UserPaidStrategy {

    void saveUserPaid(UserPaidRecordVo  userPaidRecordVo);
}
