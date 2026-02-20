package com.atfangyi.tingshu.user.mapper;

import com.atfangyi.tingshu.dto.LogDto;
import com.atfangyi.tingshu.model.user.LogInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
public interface LogInfoMapper extends BaseMapper<LogInfo> {
    Page<LogInfo> queryLogs(Page<Object> page,@Param("logDto") LogDto logDto);
}
