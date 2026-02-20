package com.atfangyi.tingshu.dispatch.service.impl;

import com.atfangyi.tingshu.dispatch.mapper.XxlJobConfigMapper;
import com.atfangyi.tingshu.dispatch.service.XxlJobConfigService;
import com.atfangyi.tingshu.model.dispatch.XxlJobConfig;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"all"})
public class XxlJobConfigServiceImpl extends ServiceImpl<XxlJobConfigMapper, XxlJobConfig> implements XxlJobConfigService {

	@Autowired
	private XxlJobConfigMapper xxlJobConfigMapper;
}
