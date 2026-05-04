package com.atfangyi.tingshu.user.client;


import cn.hutool.core.lang.Assert;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.user.AdminInfo;
import com.atfangyi.tingshu.user.client.impl.AdminDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "service-user", path = "/admin/user", fallbackFactory = AdminDegradeFeignClient.class)
public interface AdminFeignClient {


    @PostMapping("/{id}")
    AdminInfo queryAdminInfoById(@PathVariable Long id);


    @GetMapping("/internal/user/count")
    Result<Long> getUserInfoCount();


}
