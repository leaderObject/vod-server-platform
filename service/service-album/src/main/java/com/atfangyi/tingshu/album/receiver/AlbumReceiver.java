package com.atfangyi.tingshu.album.receiver;

import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.service.AlbumInfoService;
import com.atfangyi.tingshu.album.service.TrackInfoService;
import com.atfangyi.tingshu.common.constant.KafkaConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.vo.album.TrackStatMqVo;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * @Author:  方毅
 * @date:  2025/10/30 16:28
 */
@Component
@Slf4j
public class AlbumReceiver {

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private TrackInfoService trackInfoService;


    @KafkaListener(topics = KafkaConstant.QUEUE_TRACK_STAT_UPDATE)
    public void receiver(ConsumerRecord<String, String> record) {
        String value = record.value();
        if (value != null) {
            TrackStatMqVo trackStatMqVo = JSONObject.parseObject(value, TrackStatMqVo.class);
            trackInfoService.receiver(trackStatMqVo);
        }
    }

    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_REMOVE)
    public void RemoveAlbumReceiver(ConsumerRecord<String, String> record) {
        String value = record.value();
        log.info("接收到专辑删除消息：{}", value);
        if (value == null) throw new GuiguException(ResultCodeEnum.ARGUMENT_VALID_ERROR);
        albumInfoService.removeById(Long.valueOf(value ));

    }
}
