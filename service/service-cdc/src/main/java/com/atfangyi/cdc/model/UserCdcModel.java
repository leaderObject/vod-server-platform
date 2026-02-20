package com.atfangyi.cdc.model;

import lombok.Data;

import javax.persistence.Column;

/*
 * @Author:  方毅
 * @date:  2025/11/3 15:14
 */
@Data
public class UserCdcModel {

    @Column(name = "id")
    private Long id;
}
