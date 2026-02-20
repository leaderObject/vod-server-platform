package com.atfangyi.cdc.handler;

/*
 * @Author:  方毅
 * @date:  2025/11/3 15:15
 */

import com.atfangyi.cdc.model.UserCdcModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

@CanalTable(value = "user_info")
@Slf4j
@Component
public class UserCdcHandler implements EntryHandler<UserCdcModel> {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void update(UserCdcModel before, UserCdcModel after) {
        log.info("用户更改了数据{}", after);
        redisTemplate.delete("UserInfo:" + after.getId());
    }

    @Override
    public void delete(UserCdcModel userCdcModel) {
        redisTemplate.delete("UserInfo:" + userCdcModel.getId());
    }
}
