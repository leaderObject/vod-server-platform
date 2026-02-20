package com.atfangyi.cdc.handler;

import com.atfangyi.cdc.model.AlbumInfoCdcModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

/*
 * @Author:  方毅
 * @date:  2025/11/4 14:40
 */
@Component
@Slf4j
@CanalTable("album_info")
public class AlbumInfoCdcHandler implements EntryHandler<AlbumInfoCdcModel> {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void update(AlbumInfoCdcModel before, AlbumInfoCdcModel after) {
        log.info("用户更改了数据{}", after);
        redisTemplate.delete("AlbumInfo:" + after.getId());
    }

    @Override
    public void delete(AlbumInfoCdcModel albumInfoCdcModel) {
        redisTemplate.delete("UserInfo:" + albumInfoCdcModel.getId());
    }
}
