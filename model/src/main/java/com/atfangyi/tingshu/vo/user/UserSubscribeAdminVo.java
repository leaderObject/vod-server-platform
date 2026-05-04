package com.atfangyi.tingshu.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "订阅管理视图对象")
public class UserSubscribeAdminVo {

    @Schema(description = "订阅记录ID")
    private String id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "专辑ID")
    private Long albumId;

    @Schema(description = "专辑名称")
    private String albumTitle;

    @Schema(description = "专辑封面")
    private String coverUrl;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @Schema(description = "订阅时间")
    private Date createTime;

}
