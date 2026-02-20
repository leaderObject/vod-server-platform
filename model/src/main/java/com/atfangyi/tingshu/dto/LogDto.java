package com.atfangyi.tingshu.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@Data
public class LogDto {
    private String operatorType; // '1','2','3','4'
    private String operatorName;
    private String beginTime;
    private String endTime;
}
