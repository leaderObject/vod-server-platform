package com.atfangyi.tingshu.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "VIP会员视图对象")
public class VipUserVo {

    @Schema(description = "VIP记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "到期时间")
    private Date expireTime;

    @Schema(description = "是否自动续费")
    private Integer isAutoRenew;

    @Schema(description = "下次自动续费时间")
    private Date nextRenewTime;

}
