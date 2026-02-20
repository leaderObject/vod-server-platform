package com.atfangyi.tingshu.user.listener;

import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.common.ParamAssert.ServiceAssert;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.model.user.AdminInfo;
import com.atfangyi.tingshu.model.user.LogInfo;
import com.atfangyi.tingshu.user.service.AdminInfoService;
import com.atfangyi.tingshu.user.service.LogInfoService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@Component
@AllArgsConstructor
public class KafkaLogListener {

    protected static final Logger log = LoggerFactory.getLogger(KafkaLogListener.class);

    private final LogInfoService logInfoService;

    private final AdminInfoService adminInfoService;


    @KafkaListener(topics = "tingshu.oplog", groupId = "tingshu.oplog.group")
    public void add(String json) {
        LogInfo logInfo = JSONObject.parseObject(json, LogInfo.class);
        if (logInfo.getId() == null) {
            log.error("用户异常{}", logInfo);
            throw new GuiguException(ResultCodeEnum.FAIL);
        }
        logInfo.setOperatorId(logInfo.getId());
        logInfo.setId(null);
        log.info("kafka 监听到日志 {}", logInfo);
        AdminInfo adminInfo = adminInfoService.getById(logInfo.getOperatorId());
        ServiceAssert.ObjectAssert(adminInfo);
        logInfo.setOperatorName(adminInfo.getUsername());
        log.info("最终对象{}", logInfo);
        logInfoService.save(logInfo);
    }

}
