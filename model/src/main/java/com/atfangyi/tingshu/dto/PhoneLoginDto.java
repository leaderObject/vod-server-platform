package com.atfangyi.tingshu.dto;

import lombok.Data;



/**
 * @className: PhoneLoginDrto
 * @author: 方毅
 * @date: 2025/12/10 23:46
 * @version: 1.0
 * @description: TODO
 */

@SuppressWarnings({"all"})
@Data
public class PhoneLoginDto {
    private String phone;

    private String code;
}
