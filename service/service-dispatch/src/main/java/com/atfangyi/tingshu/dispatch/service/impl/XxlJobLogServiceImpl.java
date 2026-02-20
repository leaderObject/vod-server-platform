package com.atfangyi.tingshu.dispatch.service.impl;

import com.atfangyi.tingshu.dispatch.mapper.XxlJobLogMapper;
import com.atfangyi.tingshu.dispatch.service.XxlJobLogService;
import com.atfangyi.tingshu.model.dispatch.XxlJobLog;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"all"})
public class XxlJobLogServiceImpl extends ServiceImpl<XxlJobLogMapper, XxlJobLog> implements XxlJobLogService {

	@Autowired
	private XxlJobLogMapper xxlJobLogMapper;

}
