package com.atfangyi.tingshu.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
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
@TableName("log_info")
public class LogInfo {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String methodName;
    private String operatorType; // '1','2','3','4'
    private Long operatorId;
    private String operatorName;
    private String operateIp;
    private Date operateTime;
    private String extrParams;

}
