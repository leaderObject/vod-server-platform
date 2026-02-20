package com.atfangyi.tingshu.common.entity;

import lombok.Data;

import java.time.Instant;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@Data
public class Log {

    private Long id;
    private String operatorName;
    private String operatorType; // "1"/"2"/"3"/"4"
    private String methodName;
    private String operateIp;
    private String operateTime;
    private String extrParams;
}
