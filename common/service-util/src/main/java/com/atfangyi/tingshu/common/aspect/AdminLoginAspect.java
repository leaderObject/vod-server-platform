package com.atfangyi.tingshu.common.aspect;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.util.AdminAuthContextHolder;
import com.atfangyi.tingshu.common.util.JwtUtil;
import com.atfangyi.tingshu.model.user.AdminInfo;
import io.jsonwebtoken.Claims;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
 * @Author:  方毅
 * @date:  2025/11/14 22:59
 */
@Slf4j
@Aspect
@Component
public class AdminLoginAspect {


    @Autowired
    private RedisTemplate redisTemplate;


    @Around(value = " @annotation(adminLogin)")
    @SneakyThrows
    public Object adminLogin(ProceedingJoinPoint joinPoint, AdminLogin adminLogin) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        String token = servletRequestAttributes.getRequest().getHeader("token");
        Claims claims = JwtUtil.parseJWT(token);
        Object o = claims.get("sub");
        JSONObject jsonObject = JSONObject.parseObject(o.toString());
        log.info("jsonObject{}",jsonObject);
        Object id = jsonObject.get("id");
        AdminAuthContextHolder.setUserId(Long.valueOf(id.toString()));
        Object proceed = joinPoint.proceed();
        return proceed;
    }


}
