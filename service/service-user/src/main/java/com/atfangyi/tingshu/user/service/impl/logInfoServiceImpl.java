package com.atfangyi.tingshu.user.service.impl;

import com.atfangyi.tingshu.dto.LogDto;
import com.atfangyi.tingshu.model.user.LogInfo;
import com.atfangyi.tingshu.user.mapper.LogInfoMapper;
import com.atfangyi.tingshu.user.service.LogInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@Service
@AllArgsConstructor
@Slf4j
public class logInfoServiceImpl extends ServiceImpl<LogInfoMapper, LogInfo> implements LogInfoService {
    private final LogInfoMapper logInfoMapper;

    @Override
    public void add(LogInfo logInfo) {
        logInfoMapper.insert(logInfo);
    }

    @Override
    public Page<LogInfo> queryLogs(LogDto logDto, Long current, Long size) {
        log.info("接收参数{}",logDto);
        return logInfoMapper.queryLogs(Page.of(current,size), logDto);
    }
}
