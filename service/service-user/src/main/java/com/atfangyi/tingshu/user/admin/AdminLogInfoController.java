package com.atfangyi.tingshu.user.admin;

import com.atfangyi.tingshu.common.entity.Log;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.dto.LogDto;
import com.atfangyi.tingshu.model.user.LogInfo;
import com.atfangyi.tingshu.user.service.LogInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@RestController
@RequestMapping("/admin/user/log")
@Tag(name = "日志管理")

public class AdminLogInfoController {

    @Resource
    private LogInfoService logInfoService;

    @Operation(summary = "获取所有日志")
    @PostMapping("/queryLogs/{current}/{size}")
    public Result<Page<LogInfo>> queryLogs(@RequestBody(required = false) LogDto logDto,
                                           @PathVariable Long current,
                                           @PathVariable Long size) {

        return Result.ok(logInfoService.queryLogs(logDto, current, size));
    }


}
