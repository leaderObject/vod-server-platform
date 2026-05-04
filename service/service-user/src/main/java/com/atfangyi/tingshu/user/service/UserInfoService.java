package com.atfangyi.tingshu.user.service;

import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.vo.user.UserInfoVo;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import com.atfangyi.tingshu.vo.user.UserSubscribeVo;
import com.atfangyi.tingshu.vo.user.UserCollectVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> wxLogin(String code);


    UserInfo getUserInfo(String token);

    boolean updateUser(UserInfoVo userInfoVo,String token);

    Boolean isPaidAlbum(Long albumId);

    Map<Long, Object> isPaidTrack(List<Long> ids);

    UserInfo queryUserInfoByUserId(Long userId);

    void saveUserPaid(UserPaidRecordVo userPaidRecordVo);

    UserVipService userVipService();

    void queryUserVipStatus();

    List<UserInfo> queryAllUserInfo();


    UserInfo queryUserInfoById(Long id);

    IPage<UserSubscribeVo> findUserSubscribePage(Long page, Long limit);

    Boolean subscribe(Long albumId);

    Boolean isSubscribe(Long albumId);

    IPage<UserCollectVo> findUserCollectPage(Long page, Long limit);

    Boolean collect(Long trackId);

    Boolean isCollect(Long trackId);

}
