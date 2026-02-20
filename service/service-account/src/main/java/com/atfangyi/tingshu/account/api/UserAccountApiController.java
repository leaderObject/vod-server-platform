package com.atfangyi.tingshu.account.api;

import com.atfangyi.tingshu.account.service.UserAccountService;
import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.vo.account.AccountLockVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "用户账户管理")
@RestController
@RequestMapping("api/account")
@SuppressWarnings({"all"})
public class UserAccountApiController {

    @Autowired
    private UserAccountService userAccountService;


    @Operation(summary = "获取账户可用余额")
    @GetMapping("/userAccount/getAvailableAmount")
    @Login(required = true)
    public Result<BigDecimal> getAvailableAmount() {
        return Result.ok(userAccountService.getAvailableAmount(AuthContextHolder.getUserId()));
    }


    @Operation(summary = "检查及锁定账户金额")
    @PostMapping("/userAccount/checkAndLock")
    public Result<AccountLockVo> checkAndLock(@RequestBody AccountLockVo accountLockVo) {
        return Result.ok(userAccountService.checkAndLock(accountLockVo));
    }

    @Operation(summary = "用户充值金额记录")
    @PostMapping("/userAccount/recharge/{orderNo}")
    @Login
    public  void  recharge(@PathVariable String orderNo) {
        userAccountService.recharge(orderNo);
    }

}

