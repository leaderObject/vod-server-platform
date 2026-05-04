package com.atfangyi.tingshu.user.admin;

import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.user.UserVipService;
import com.atfangyi.tingshu.model.user.VipServiceConfig;
import com.atfangyi.tingshu.user.mapper.UserVipServiceMapper;
import com.atfangyi.tingshu.user.service.VipServiceConfigService;
import com.atfangyi.tingshu.user.service.UserInfoService;
import com.atfangyi.tingshu.vo.user.VipUserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Tag(name = "VIP会员管理")
@RestController
@RequestMapping("/admin/vip")
@SuppressWarnings({"all"})
public class AdminVipController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private VipServiceConfigService vipServiceConfigService;

    @Resource
    private UserVipServiceMapper userVipServiceMapper;

    @Operation(summary = "获取VIP套餐列表")
    @GetMapping("/getVipConfigList")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "getVipConfigList")
    public Result<List> getVipConfigList() {
        return Result.ok(vipServiceConfigService.list());
    }

    @Operation(summary = "分页查询VIP会员列表")
    @GetMapping("/getVipUserPage/{page}/{limit}")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "getVipUserPage")
    public Result<IPage<VipUserVo>> getVipUserPage(@PathVariable Long page, @PathVariable Long limit) {
        // 查询所有VIP用户
        List<VipUserVo> allVipUsers = new ArrayList<>();

        // 从user_vip_service表查询VIP记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserVipService> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserVipService::getExpireTime);

        List<UserVipService> vipServiceList = userInfoService.getVipServiceList(wrapper);

        for (UserVipService vipService : vipServiceList) {
            VipUserVo vo = new VipUserVo();
            vo.setId(vipService.getId());
            vo.setUserId(vipService.getUserId());
            vo.setOrderNo(vipService.getOrderNo());
            vo.setStartTime(vipService.getStartTime());
            vo.setExpireTime(vipService.getExpireTime());
            vo.setIsAutoRenew(vipService.getIsAutoRenew());
            vo.setNextRenewTime(vipService.getNextRenewTime());

            // 获取用户信息
            com.atfangyi.tingshu.model.user.UserInfo userInfo = userInfoService.queryUserInfoById(vipService.getUserId());
            if (userInfo != null) {
                vo.setNickname(userInfo.getNickname());
                vo.setAvatarUrl(userInfo.getAvatarUrl());
                vo.setPhone(userInfo.getPhone());
            }

            allVipUsers.add(vo);
        }

        // 分页处理
        int start = (int) ((page - 1) * limit);
        int end = (int) Math.min(start + limit, allVipUsers.size());

        List<VipUserVo> pageList = start < allVipUsers.size() ? allVipUsers.subList(start, end) : new ArrayList<>();

        Page<VipUserVo> pageResult = new Page<>(page, limit, allVipUsers.size());
        pageResult.setRecords(pageList);

        return Result.ok(pageResult);
    }

    @Operation(summary = "获取VIP会员总数")
    @GetMapping("/getVipUserCount")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "getVipUserCount")
    public Result<Long> getVipUserCount() {
        LambdaQueryWrapper<UserVipService> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(UserVipService::getExpireTime, new java.util.Date());
        long count = userInfoService.getVipServiceCount(wrapper);
        return Result.ok(count);
    }

    @Operation(summary = "开通/续费VIP会员")
    @PostMapping("/openVip")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "openVip")
    public Result<String> openVip(@RequestParam Long userId, @RequestParam Long vipConfigId) {
        // 获取VIP套餐配置
        VipServiceConfig vipConfig = vipServiceConfigService.getById(vipConfigId);
        if (vipConfig == null) {
            return Result.fail("VIP套餐不存在");
        }

        // 查询用户当前VIP状态
        LambdaQueryWrapper<UserVipService> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVipService::getUserId, userId);
        wrapper.gt(UserVipService::getExpireTime, new Date());
        UserVipService existingVip = userVipServiceMapper.selectOne(wrapper);

        Date now = new Date();
        Date newExpireTime;

        if (existingVip != null) {
            // 已有VIP会员，叠加天数
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(existingVip.getExpireTime());
            calendar.add(Calendar.MONTH, vipConfig.getServiceMonth());
            newExpireTime = calendar.getTime();

            // 更新现有记录
            existingVip.setExpireTime(newExpireTime);
            existingVip.setIsAutoRenew(1);
            userVipServiceMapper.updateById(existingVip);
        } else {
            // 无VIP或已过期，创建新记录
            UserVipService newVip = new UserVipService();
            newVip.setUserId(userId);
            newVip.setStartTime(now);
            newVip.setIsAutoRenew(1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MONTH, vipConfig.getServiceMonth());
            newExpireTime = calendar.getTime();
            newVip.setExpireTime(newExpireTime);

            userVipServiceMapper.insert(newVip);
        }

        return Result.ok("会员开通成功，到期时间：" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newExpireTime));
    }
}
