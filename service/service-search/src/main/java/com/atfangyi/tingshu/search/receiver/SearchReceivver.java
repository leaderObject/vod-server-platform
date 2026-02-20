package com.atfangyi.tingshu.search.receiver;

import com.atfangyi.tingshu.common.constant.KafkaConstant;
import com.atfangyi.tingshu.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * @Author:  方毅
 * @date:  2025/10/25 14:03
 */
@Component
@Slf4j
@SuppressWarnings({"all"})
public class SearchReceivver {

    @Autowired
    private SearchService searchService;

    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_LOWER)
    public void AlbumLower(ConsumerRecord<String, String> record) {
        log.info("接收到专辑下架消息：{}", record.value());
        searchService.AlbumLower(record.value());
    }

    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_UPPER)
    public void AlbumUpper(ConsumerRecord<String, String> record) {
        log.info("接收到专辑更新消息：{}", record.value());
        searchService.AlbumUpper(record.value());

    }
}
