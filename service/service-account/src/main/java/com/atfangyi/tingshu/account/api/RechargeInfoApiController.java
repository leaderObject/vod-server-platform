package com.atfangyi.tingshu.account.api;

import com.atfangyi.tingshu.account.mapper.UserAccountDetailMapper;
import com.atfangyi.tingshu.account.service.RechargeInfoService;
import com.atfangyi.tingshu.account.service.UserAccountService;
import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.model.account.RechargeInfo;
import com.atfangyi.tingshu.model.account.UserAccountDetail;
import com.atfangyi.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "充值管理")
@RestController
@RequestMapping("api/account")
@SuppressWarnings({"all"})
public class RechargeInfoApiController {

	@Autowired
	private RechargeInfoService rechargeInfoService;

	@Autowired
	private UserAccountDetailMapper userAccountDetailMapper;

	@Operation(summary = "充值余额")
	@PostMapping("/rechargeInfo/submitRecharge")
	@Login
	public Result<Map<String,String>> submitRecharge(@RequestBody RechargeInfoVo rechargeInfoVo ) {
		return Result.ok(rechargeInfoService.submitRecharge(rechargeInfoVo));
	}


	@Operation(summary = "根据订单号获取充值信息")
	@GetMapping("/rechargeInfo/getRechargeInfo/{orderNo}")
	public  Result<RechargeInfo> getRechargeInfo(@PathVariable String orderNo){
		  return  Result.ok(rechargeInfoService.getOne(Wrappers.lambdaQuery(RechargeInfo.class).eq(RechargeInfo::getOrderNo,orderNo)));
	}



	@Operation(summary = "修改充值状态")
	@GetMapping("/userAccount/UpdateStatus/{orderNo}")
	public  void   UpdateStatus(@PathVariable String orderNo){
		      rechargeInfoService.UpdateStatus(orderNo);
	}

	@Operation(summary = "获取充值记录")
	@GetMapping("/userAccount/findUserRechargePage/{page}/{limit}")
	@Login
	public  Result<Page<UserAccountDetail>> findUserRechargePage(@PathVariable Long page, @PathVariable Long limit){
		Page<UserAccountDetail> userAccountDetailPage = new Page<>(page, limit);
		LambdaQueryWrapper<UserAccountDetail> userAccountDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
		userAccountDetailLambdaQueryWrapper.eq(UserAccountDetail::getUserId,AuthContextHolder.getUserId());
		userAccountDetailLambdaQueryWrapper.eq(UserAccountDetail::getTradeType, SystemConstant.ACCOUNT_TRADE_TYPE_DEPOSIT);
		return  Result.ok(userAccountDetailMapper.selectPage(userAccountDetailPage,userAccountDetailLambdaQueryWrapper));
	}

	@Operation(summary = "获取消费记录")
	@GetMapping("/userAccount/findUserConsumePage/{page}/{limit}")
	@Login
	public  Result<Page<UserAccountDetail>> findUserConsumePage(@PathVariable Long page, @PathVariable Long limit){
		Page<UserAccountDetail> userAccountDetailPage = new Page<>(page, limit);
		LambdaQueryWrapper<UserAccountDetail> userAccountDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
		userAccountDetailLambdaQueryWrapper.eq(UserAccountDetail::getUserId,AuthContextHolder.getUserId());
		userAccountDetailLambdaQueryWrapper.eq(UserAccountDetail::getTradeType, SystemConstant.ACCOUNT_TRADE_TYPE_MINUS);
		return  Result.ok(userAccountDetailMapper.selectPage(userAccountDetailPage,userAccountDetailLambdaQueryWrapper));
	}
}

