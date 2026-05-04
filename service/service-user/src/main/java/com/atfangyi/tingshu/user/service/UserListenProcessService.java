package com.atfangyi.tingshu.user.service;

import com.atfangyi.tingshu.vo.user.UserListenProcessVo;
import com.atfangyi.tingshu.vo.user.UserListenProcessListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;

public interface UserListenProcessService {

    BigDecimal getTrackBreakSecondBytrackId(Long trackId);

    void updateListenProcess(UserListenProcessVo userListenProcessVo);

    IPage<UserListenProcessListVo> findUserPage(Long page, Long limit);

    void deleteById(String id);

    UserListenProcessVo getLatelyTrack();
}
