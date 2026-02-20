package com.atfangyi.tingshu.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserInfoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String phone;

    private String nickname;

    private String avatarUrl;

    private Integer isVip;

    private Date vipExpireTime;

    private Integer gender;

    private String status;

}
