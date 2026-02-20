package com.atfangyi.tingshu.user.client.impl;


import com.atfangyi.tingshu.model.user.AdminInfo;
import com.atfangyi.tingshu.user.client.AdminFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminDegradeFeignClient implements FallbackFactory<AdminFeignClient> {

    @Override
    public AdminFeignClient create(Throwable cause) {
        return new AdminFeignClient() {
            @Override
            public AdminInfo queryAdminInfoById(Long id) {
                log.error("queryAdminInfoById降级处理 --{}", cause.getMessage());
                return null;
            }

            @Override
            public Long getUserInfoCount() {
                log.error("getUserInfoCount降级处理 --{}", cause.getMessage());
                return 0L;
            }
        };
    }
}
