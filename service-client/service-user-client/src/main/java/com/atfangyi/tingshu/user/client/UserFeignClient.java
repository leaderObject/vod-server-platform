package com.atfangyi.tingshu.user.client;

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.model.user.VipServiceConfig;
import com.atfangyi.tingshu.user.client.impl.UserDegradeFeignClient;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author atguigu
 */
@FeignClient(value = "service-user", path = "/api/user", fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {


    @GetMapping("/queryUserInfoByUserId/{userId}")
    UserInfo queryUserInfoByUserId(@PathVariable Long userId);


    @GetMapping("/userInfo/isPaidAlbum/{albumId}")
    Result<Boolean> isPaidAlbum(@PathVariable Long albumId);

    @PostMapping("/userInfo/isPaidTrack")
    Result<Map<Long, Object>> isPaidTrack(@RequestBody List<Long> ids);

    @GetMapping("/vipServiceConfig/getVipServiceConfig/{id}")
    Result<VipServiceConfig> getVipServiceConfig(@PathVariable Long id);

    @PostMapping("/userInfo/UserPaid")
    void saveUserPaid(@RequestBody UserPaidRecordVo userPaidRecordVo);

    @GetMapping("/userinfo/CheckUserVipstatus")
    UserVipService userVipService();

    @GetMapping("/queryUserVipStatus")
    void queryUserVipStatus();


}
