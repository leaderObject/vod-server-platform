package com.atfangyi.tingshu.user.service;

import com.atfangyi.tingshu.vo.user.UserListenProcessVo;

import java.math.BigDecimal;

public interface UserListenProcessService {

    BigDecimal getTrackBreakSecondBytrackId(Long trackId);

    void updateListenProcess(UserListenProcessVo userListenProcessVo);
}
