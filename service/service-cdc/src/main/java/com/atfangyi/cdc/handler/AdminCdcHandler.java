package com.atfangyi.cdc.handler;


import com.atfangyi.cdc.model.AdminInfoCdcModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import javax.annotation.Resource;

@CanalTable("admin_info")
@Component
@Slf4j
public class AdminCdcHandler implements EntryHandler<AdminInfoCdcModel> {

    protected static final String ADMIN_INFO_PREFIX = "adminInfo:";

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void update(AdminInfoCdcModel before, AdminInfoCdcModel after) {
        log.debug("用户修改了数据{}", after);
        redisTemplate.delete(ADMIN_INFO_PREFIX + after.getId());
    }

    @Override
    public void delete(AdminInfoCdcModel adminInfoCdcModel) {
        log.debug("用户删除了数据{}", adminInfoCdcModel);
        redisTemplate.delete(ADMIN_INFO_PREFIX + adminInfoCdcModel.getId());
    }
}
