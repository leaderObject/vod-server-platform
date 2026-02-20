package com.atfangyi.tingshu;

import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.search.repository.AlbumInfoIndexRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@Slf4j
public class ServiceSearchApplication {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AlbumInfoIndexRepository albumInfoIndexRepository;


    public static void main(String[] args) {
        SpringApplication.run(ServiceSearchApplication.class, args);
    }

    @PostConstruct
    public void BloomFilterInit() {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        bloomFilter.tryInit(1000000, 0.01);
        log.info("bloom init success");
        albumInfoIndexRepository.findAll().forEach(albumInfoIndex -> bloomFilter.add(albumInfoIndex.getId()));

    }
}
