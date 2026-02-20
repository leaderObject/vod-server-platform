package com.atfangyi.tingshu.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.model.user.UserInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
 * @Author:  方毅
 * @date:  2025/10/20 13:21
 */
@Aspect
@Component
@Slf4j
public class LoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;


    @Pointcut("execution(* com.atfangyi.tingshu.*.*..*Controller.*(..))")
    public void pointcut() {
    }

    @Around(value = "pointcut()  && @annotation(login)")
    @SneakyThrows
    public Object login(ProceedingJoinPoint joinPoint, Login login) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        String token = servletRequestAttributes.getRequest().getHeader("token");
        Object object = redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        UserInfo userInfo = null;
        if (object != null) {
            userInfo = (UserInfo) object;
        }
        //认证且用户未登录
        if (login.required() && userInfo == null) {
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        if (!login.required()) {
            if (userInfo == null) AuthContextHolder.setUserId(1l);
        }
        if (userInfo != null) AuthContextHolder.setUserId(userInfo.getId());
        Object proceed = joinPoint.proceed();
        AuthContextHolder.removeUserId();
        return proceed;
    }
}
