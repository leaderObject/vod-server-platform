package com.atfangyi.tingshu.account.listener;

import com.atfangyi.tingshu.account.service.UserAccountService;
import com.atfangyi.tingshu.common.constant.KafkaConstant;
import com.tencentcloudapi.cdb.v20170320.models.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * @Author:  方毅
 * @date:  2025/10/20 16:54
 */
@Component
@Slf4j
public class KafkaUserRegisterListener {

    @Autowired
    private UserAccountService userAccountService;


    @KafkaListener(topics = KafkaConstant.QUEUE_USER_REGISTER)
    public void receiver(ConsumerRecord<String, String> message) {
        log.info("接收到消息：{}", message.value());
        userAccountService.saveUserAccount(message.value());
    }
}
