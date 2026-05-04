package com.atfangyi.tingshu.user.api;

import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.user.service.UserListenProcessService;
import com.atfangyi.tingshu.vo.user.UserListenProcessVo;
import com.atfangyi.tingshu.vo.user.UserListenProcessListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
    @Login(required = true)
    public Result updateListenProcess(@RequestBody UserListenProcessVo userListenProcessVo) {
        userListenProcessService.updateListenProcess(userListenProcessVo);
        return Result.ok();
    }

    @Login
    @Operation(summary = "分页查询播放历史")
    @GetMapping("/userListenProcess/findUserPage/{page}/{limit}")
    public Result<IPage<UserListenProcessListVo>> findUserPage(@PathVariable Long page, @PathVariable Long limit) {
        IPage<UserListenProcessListVo> pageResult = userListenProcessService.findUserPage(page, limit);
        return Result.ok(pageResult);
    }

    @Login
    @Operation(summary = "删除播放历史")
    @DeleteMapping("/userListenProcess/delete/{id}")
    public Result deleteById(@PathVariable String id) {
        userListenProcessService.deleteById(id);
        return Result.ok();
    }

    @Login
    @Operation(summary = "获取最近播放记录")
    @GetMapping("/userListenProcess/getLatelyTrack")
    public Result<UserListenProcessVo> getLatelyTrack() {
        UserListenProcessVo vo = userListenProcessService.getLatelyTrack();
        return Result.ok(vo);
    }

}

