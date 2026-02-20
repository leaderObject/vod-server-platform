package com.atfangyi.tingshu.user.service;

import com.atfangyi.tingshu.dto.AdminDto;
import com.atfangyi.tingshu.dto.PhoneLoginDto;
import com.atfangyi.tingshu.model.user.AdminInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
* @author ZhuanZ（无密码）
* @description 针对表【admin_info(管理员信息)】的数据库操作Service
* @createDate 2025-12-10 17:43:47
*/
public interface AdminInfoService extends IService<AdminInfo> {

    String login(AdminDto adminDto, HttpServletRequest serverHttpRequest);


    boolean  code(String phone);


    String  phoneLogin(PhoneLoginDto  phoneLoginDto, HttpServletRequest serverHttpRequest);


    AdminInfo queryUserInfoById(Long id);

}
