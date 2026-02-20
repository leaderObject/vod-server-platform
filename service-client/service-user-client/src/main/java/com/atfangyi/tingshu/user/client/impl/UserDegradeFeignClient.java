package com.atfangyi.tingshu.user.client.impl;


import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.model.user.VipServiceConfig;
import com.atfangyi.tingshu.user.client.UserFeignClient;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserDegradeFeignClient implements UserFeignClient {

    @Override
    public UserInfo queryUserInfoByUserId(Long userId) {
        log.error("[用户模块远程调用失败执行熔断降级{}]", "queryUserInfoByUserId");
        return null;
    }

    @Override
    public Result<Boolean> isPaidAlbum(Long albumId) {
        log.error("[用户模块远程调用失败执行熔断降级{}]", "isPaidAlbum");
        return null;
    }

    @Override
    public Result<Map<Long, Object>> isPaidTrack(List<Long> ids) {
        log.error("[用户模块远程调用失败执行熔断降级{}]", "isPaidTrack");
        return null;
    }

    @Override
    public Result<VipServiceConfig> getVipServiceConfig(Long id) {
        log.error("[用户模块远程调用失败执行熔断降级{}]", "getVipServiceConfig");
        return null;
    }

    @Override
    public void saveUserPaid(UserPaidRecordVo userPaidRecordVo) {
        log.error("[用户模块远程调用失败执行熔断降级{}]", "saveUserPaid");

    }

    @Override
    public UserVipService userVipService() {
        log.error("[用户模块远程调用失败执行熔断降级{}]", "userVipService");
        return null;
    }

    @Override
    public void queryUserVipStatus() {
        log.error("[用户模块远程调用失败执行熔断降级{}]", "queryUserVipStatus");
    }


}
