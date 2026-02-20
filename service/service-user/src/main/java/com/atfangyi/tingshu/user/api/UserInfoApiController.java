package com.atfangyi.tingshu.user.api;

import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.user.mapper.UserVipServiceMapper;
import com.atfangyi.tingshu.user.service.UserInfoService;
import com.atfangyi.tingshu.vo.user.UserInfoVo;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("api/user")
@SuppressWarnings({"all"})
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;




    @Operation(summary = "更新用户信息")
    @PostMapping("/wxLogin/updateUser")
    @Login(required = true)
    public Result updateUser(@RequestBody UserInfoVo userInfoVo, @RequestHeader("token") String token) {
        return userInfoService.updateUser(userInfoVo, token) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "获取登录用户信息")
    @GetMapping("/queryUserInfoByUserId/{userId}")
    public UserInfo queryUserInfoByUserId(@PathVariable Long userId) {
        return userInfoService.queryUserInfoByUserId(userId);
    }

    @Operation(summary = "判断用户是否购买过指定专辑")
    @GetMapping("/userInfo/isPaidAlbum/{albumId}")
    @Login
    public Result<Boolean> isPaidAlbum(@PathVariable Long albumId) {
        return Result.ok(userInfoService.isPaidAlbum(albumId));
    }

    @Operation(summary = "判断用户是否购买过声音列表")
    @PostMapping("/userInfo/isPaidTrack")
    @Login
    public Result<Map<Long, Object>> isPaidTrack(@RequestBody List<Long> ids) {
        return Result.ok(userInfoService.isPaidTrack(ids));
    }

    @Operation(summary = "编辑用户付款项目")
    @PostMapping("/userInfo/UserPaid")
    @Login
    public  void  saveUserPaid(@RequestBody UserPaidRecordVo userPaidRecordVo) {
        userInfoService.saveUserPaid(userPaidRecordVo);
    }

    @Operation(summary = "检查用户是否为vip")
    @GetMapping("/userinfo/CheckUserVipstatus")
    @Login(required = true)
    public UserVipService userVipService(){
          return  userInfoService.userVipService();
    }

    @Operation(summary = "查看当天用户vip状态")
    @GetMapping("/queryUserVipStatus")
    @Login
    public  void   queryUserVipStatus(){
        userInfoService.queryUserVipStatus();
    }

}

