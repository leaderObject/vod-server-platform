package com.atfangyi.tingshu.user.api;

import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.model.user.VipServiceConfig;
import com.atfangyi.tingshu.user.mapper.UserVipServiceMapper;
import com.atfangyi.tingshu.user.service.VipServiceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "VIP服务配置管理接口")
@RestController
@RequestMapping("api/user")
@SuppressWarnings({"all"})
public class VipServiceConfigApiController {

    @Autowired
    private VipServiceConfigService vipServiceConfigService;

    @Autowired
    private UserVipServiceMapper   userVipServiceMapper;


    @Operation(summary = "获取全部VIP会员服务配置信息")
    @GetMapping("/vipServiceConfig/findAll")
    public Result<List<VipServiceConfig>> vipServiceConfigQueryAll() {
        return Result.ok(vipServiceConfigService.list());
    }

    @Operation(summary = "根据id获取VIP服务配置信息")
    @GetMapping("/vipServiceConfig/getVipServiceConfig/{id}")
    public Result<VipServiceConfig> getVipServiceConfig(@PathVariable Long id) {
        return Result.ok(vipServiceConfigService.getById(id));
    }



}

