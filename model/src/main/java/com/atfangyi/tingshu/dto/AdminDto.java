package com.atfangyi.tingshu.dto;

import lombok.Data;


/**
 * @className: AdminDto
 * @author: 方毅
 * @date: 2025/12/10 18:28
 * @version: 1.0
 * @description: TODO
 */

@SuppressWarnings({"all"})
@Data
public class AdminDto {

    private Long id;

    private String username;

    private String password;

    private String code;

    private Integer gender;

    private String avatarUrl;

    private String introduce;


}
