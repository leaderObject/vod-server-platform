package com.atfangyi.tingshu.user.admin;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.common.util.AdminAuthContextHolder;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.common.util.HttpClientUtils;
import com.atfangyi.tingshu.dto.AdminDto;
import com.atfangyi.tingshu.dto.PhoneLoginDto;
import com.atfangyi.tingshu.dto.UserInfoDto;
import com.atfangyi.tingshu.model.user.AdminInfo;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.user.properties.WxLoginConfigProperties;
import com.atfangyi.tingshu.user.service.AdminInfoService;
import com.atfangyi.tingshu.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @className: AdminInfoController
 * @author: 方毅
 * @date: 2025/12/10 18:30
 * @version: 1.0
 * @description: TODO
 */
@SuppressWarnings({"all"})
@RestController
@RequestMapping("/admin/user")
@Tag(name = "管理员")
@Slf4j
public class AdminInfoController {

    @Resource
    private AdminInfoService adminInfoService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private WxLoginConfigProperties wxLoginConfigProperties;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/login")
    @Operation(summary = "登录接口")
    public Result login(@RequestBody AdminDto adminDto, HttpServletRequest serverHttpRequest) {
        return Result.ok(adminInfoService.login(adminDto, serverHttpRequest));
    }


    @PostMapping("/phone/code")
    @Operation(summary = "获取验证码")
    public Result code(@RequestParam String phone) {
        Long userId = AdminAuthContextHolder.getUserId();
        return Result.ok(adminInfoService.code(phone));
    }


    @PostMapping("/phone/login")
    @Operation(summary = "手机号登录")
    public Result phoneLogin(@RequestBody PhoneLoginDto phoneLoginDto, HttpServletRequest serverHttpRequest) {
        return Result.ok(adminInfoService.phoneLogin(phoneLoginDto, serverHttpRequest));
    }


    @PostMapping("/queryAdminInfoById")
    @Operation(summary = "查询管理员信息")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4",operatorMethod = "queryUserInfoById")

    public Result<AdminInfo> queryUserInfoById() {
        return Result.ok(adminInfoService.queryUserInfoById(AdminAuthContextHolder.getUserId()));
    }


    @Operation(summary = "获取用户总数")
    @GetMapping("/getUserInfoCount")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4",operatorMethod = "getUserInfoCount")
    public Long getUserInfoCount() {
        return userInfoService.count();
    }

    /**
     * 内部接口，供其他微服务调用，无需认证
     */
    @Operation(summary = "内部接口-获取用户总数")
    @GetMapping("/internal/user/count")
    public Result<Long> getUserCountInternal() {
        return Result.ok(userInfoService.count());
    }

    @GetMapping("/wechat/qrcode")
    @Operation(summary = "微信扫码登录接口")
    public Result<String> wechatQrcode() {
        String WxLoginUrl = "https://open.weixin.qq.com/connect/qrconnect" +
                "?appid=%s"
                + "&redirect_uri=%s"
                + "&response_type=code"
                + "&scope=snsapi_login";
        return Result.ok(String.format(WxLoginUrl, wxLoginConfigProperties.getAppId(), wxLoginConfigProperties.getRedirectUrl()));

    }

    @GetMapping("/wechat/redirect")
    @Operation(summary = "微信扫码登录接口")
    public void wechatRedirect(@RequestParam String code, @RequestParam String state) {
        log.info("code={}", code);
        log.info("state={}", state);
    }


    @PostMapping("/queryAllUserInfo")
    @Operation(summary = "查询所有用户的信息")
    @OperatorLogAnnotation(operatorType = "4",operatorMethod = "queryAllUserInfo")
    @AdminLogin
    public Result<List<UserInfo>> queryAllUserInfo() {
        return Result.ok(userInfoService.queryAllUserInfo());
    }

    @PostMapping("/queryUserInfoById/{id}")
    @Operation(summary = "根据Id查询用户信息")
    @OperatorLogAnnotation(operatorType = "4",operatorMethod = "queryUserInfoById")
    @AdminLogin
    public Result<UserInfo> queryUserInfoById(@PathVariable Long id) {
        UserInfo userInfo = userInfoService.queryUserInfoById(id);
        return Result.ok(userInfo);
    }

    @DeleteMapping("/removeUserById/{id}")
    @Operation(summary = "根据Id移除用户信息")
    @OperatorLogAnnotation(operatorType = "2",operatorMethod = "removeUserById")
    @AdminLogin
    public Result removeUserById(@PathVariable Long id) {
        return userInfoService.removeById(id) ? Result.ok() : Result.fail();
    }

    //
    @PutMapping("/updateUserInfoById")
    @Operation(summary = "修改用户信息")
    @OperatorLogAnnotation(operatorType = "3",operatorMethod = "updateUserInfoById")
    @AdminLogin
    public Result updateUserInfoById(@RequestBody UserInfoDto userInfoDto) {
        UserInfo userInfo = userInfoService.getOne(Wrappers.lambdaQuery(UserInfo.class).eq(UserInfo::getPhone, userInfoDto.getPhone()));
        if (userInfo != null && userInfo.getId() != userInfoDto.getId()) throw new GuiguException(500, "手机号已存在");
        return userInfoService.updateById(BeanUtil.copyProperties(userInfoDto, UserInfo.class)) ? Result.ok() : Result.fail();
    }


    @GetMapping("/logout")
    @Operation(summary = "退出登录")
    @AdminLogin
    public Result logout() {
        redisTemplate.delete(RedisConstant.ADMIN_INFO_PREFIX + AdminAuthContextHolder.getUserId());
        return Result.ok();
    }

    @PostMapping("/updateAdminInfo")
    @Operation(summary = "修改管理员信息")
    @OperatorLogAnnotation(operatorType = "3",operatorMethod = "updateAdminInfo")
    @AdminLogin
    public Result updateAdminInfo(@RequestBody AdminDto adminDto) {
        return adminInfoService.updateById(BeanUtil.copyProperties(adminDto, AdminInfo.class)) ? Result.ok() : Result.fail();
    }
}
