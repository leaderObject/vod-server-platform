package com.atfangyi.tingshu.model.user;

/*
 * @Author:  方毅
 * @date:  2025/11/14 22:16
 */

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户登录信息")
@TableName("user_login_info")
public class UserLoginInfo {

    @Schema(description = "用户ID")
    @TableId(value = "user_id")
    private Integer userId;

    @TableField(value = "username")
    @Schema(description = "用户名")
    private String username;


    @TableField(value = "password")
    @Schema(description = "用户密码")
    private String password;


    @Schema(description = "邮箱地址")
    @TableField(value = "email")
    private String email;

    @Schema(description = "手机号码")
    @TableField(value = "phone")
    private String phone;

    @Schema(description = "用户全名")
    @TableField(value = "full_name")
    private String fullName;

    @Schema(description = "最后登录时间")
    @TableField(value = "last_login")
    private LocalDateTime lastLogin;

    @Schema(description = "登录次数")
    @TableField(value = "login_count")
    private Integer loginCount;

    @Schema(description = "账户状态")
    @TableField(value = "account_status")
    private String accountStatus;

    @Schema(description = "创建时间")
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "连续登录失败次数")
    @TableField(value = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Schema(description = "最后登录失败时间")
    @TableField(value = "last_failed_login")
    private LocalDateTime lastFailedLogin;

    @Schema(description = "密码最后修改时间")
    @TableField(value = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Schema(description = "是否启用双因素认证")
    @TableField(value = "two_factor_enabled")
    private Boolean twoFactorEnabled;

    @Schema(description = "最后登录IP地址")
    @TableField(value = "ip_address")
    private String ipAddress;

    @Schema(description = "最后登录设备信息")
    @TableField(value = "user_agent")
    private String userAgent;

}
