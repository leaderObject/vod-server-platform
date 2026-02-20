package com.atfangyi.tingshu.user.aop;

import cn.hutool.core.lang.Assert;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.user.annotation.SendCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * @className: LoginAop
 * @author: 方毅
 * @date: 2025/12/10 19:09
 * @version: 1.0
 * @description: aop
 */

@SuppressWarnings({"all"})
@Slf4j
@Component
@Aspect
public class LoginAop {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 对发送的验证码做验证
     *
     * @param proceedingJoinPoint
     * @param sendCode
     * @return
     */
    @Around(value = "@annotation(sendCode)")
    public Object SendCode(ProceedingJoinPoint proceedingJoinPoint, SendCode sendCode) {
        Object[] args = proceedingJoinPoint.getArgs();
        Assert.notNull(args);
        String RedisKey = args[0] + ":" + sendCode.suffix();
        Object object = redisTemplate.opsForValue().get(RedisKey);
        Long count = 0L;
        if (object != null) {
            count = Long.valueOf(object.toString());
        }
        if (count >= 3) {
            log.error("验证码发送次数过多,,{}", count);
            throw new GuiguException(ResultCodeEnum.PHONE_CODE_MORE);
        }
        String RedisCodeKey = args[0] + ":" + "code";
        if (redisTemplate.opsForValue().get(RedisCodeKey) != null)
            throw new GuiguException(ResultCodeEnum.PHONE_CODE_FAST);
        try {
            Object proceed = proceedingJoinPoint.proceed();
            // 获取今天还剩多少秒
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
            redisTemplate.opsForValue().set(RedisKey, ++count, Duration.between(now,endOfDay).getSeconds(), TimeUnit.SECONDS);
            return proceed;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
