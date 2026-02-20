package com.atfangyi.tingshu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;


@SpringBootApplication //ComponentScan  SpringBoorConfiguration  EnableAutoConfiguration
@EnableDiscoveryClient
@EnableFeignClients
@RefreshScope
public class ServiceAlbumApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAlbumApplication.class, args);
    }


}
