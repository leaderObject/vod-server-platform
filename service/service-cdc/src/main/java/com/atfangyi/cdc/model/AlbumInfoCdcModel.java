package com.atfangyi.cdc.model;

import lombok.Data;
import top.javatool.canal.client.annotation.CanalTable;

import javax.persistence.Column;

/*
 * @Author:  方毅
 * @date:  2025/11/4 14:39
 */
@Data
public class AlbumInfoCdcModel {

    @Column(name = "id")
    private  Long id;

}
