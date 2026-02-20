package com.atfangyi.cdc.model;


import lombok.Data;

import javax.persistence.Column;

@Data
public class AdminInfoCdcModel {


    @Column(name = "id")
    private  Long id;
}
