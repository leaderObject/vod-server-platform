package com.atfangyi.tingshu.user.api;

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {

    @Autowired
    private UserInfoService userInfoService;


    @Operation(summary = "小程序授权登录")
    @GetMapping("/wxLogin/{code}")
    public Result wxLogin(@PathVariable String code) {
        return Result.ok(userInfoService.wxLogin(code));
    }

    @Operation(summary = "获取登录用户信息")
    @GetMapping("/getUserInfo")
    public Result<UserInfo> getUserInfo(@RequestHeader("token") String token) {
        return Result.ok(userInfoService.getUserInfo(token));
    }


}
