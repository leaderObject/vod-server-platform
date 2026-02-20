package com.atfangyi.tingshu.user.service;

import com.atfangyi.tingshu.dto.LogDto;
import com.atfangyi.tingshu.model.user.LogInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
public interface LogInfoService extends IService<LogInfo> {


    void add(LogInfo logInfo);

    Page<LogInfo> queryLogs(LogDto logDto, Long current, Long size);

}
