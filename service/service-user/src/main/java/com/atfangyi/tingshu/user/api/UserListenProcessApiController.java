package com.atfangyi.tingshu.user.api;

import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.user.service.UserListenProcessService;
import com.atfangyi.tingshu.vo.user.UserListenProcessVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "用户声音播放进度管理接口")
@RestController
@RequestMapping("api/user")
@SuppressWarnings({"all"})
public class UserListenProcessApiController {

    @Autowired
    private UserListenProcessService userListenProcessService;

    @Operation(summary = "获取声音的上次跳出时间")
    @GetMapping("/userListenProcess/getTrackBreakSecond/{trackId}")
    @Login(required = true)
    public Result<BigDecimal> getTrackBreakSecondBytrackId(@PathVariable Long trackId) {
        return Result.ok(userListenProcessService.getTrackBreakSecondBytrackId(trackId));
    }

    @Operation(summary = "更新播放进度")
    @PostMapping("/userListenProcess/updateListenProcess")
    @Login
    public Result updateListenProcess(@RequestBody UserListenProcessVo userListenProcessVo) {
        userListenProcessService.updateListenProcess(userListenProcessVo);
        return Result.ok();
    }

}

