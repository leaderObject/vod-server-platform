package com.atfangyi.tingshu.common.sink;

import cn.hutool.json.ObjectMapper;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.common.entity.Log;
import com.atfangyi.tingshu.common.service.KafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@Component
@RequiredArgsConstructor
public class KafkaOperationLogSink implements OperationLogSink {
    private final KafkaService kafkaService;

    @Override
    public void publish(Log log) {
        try {
            String json = JSONObject.toJSONString(log);
            String key = log.getId() != null ? String.valueOf(log.getId()) : "1";
            kafkaService.sendMessage("tingshu.oplog", key, json);
        } catch (Exception e) {

        }
    }
}
