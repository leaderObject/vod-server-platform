package com.atfangyi.tingshu.common.aspect;

/*
 * @Author:  方毅
 * @date:  2025/11/2 18:04
 */

import cn.hutool.core.util.IdUtil;
import com.atfangyi.tingshu.common.annotation.Cache;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Aspect
@Component
public class CacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut(value = "execution(* com.atfangyi.tingshu.*.service.*.*(..))")
    public void pointCut() {
    }

    @Around(value = "pointCut() && @annotation(cache)")
    @SuppressWarnings({ "all" })
    public Object around(ProceedingJoinPoint proceedingJoinPoint, Cache cache) {
        try {
            String prefix = cache.prefix();
            String RedisKey = prefix +":"+ Arrays.asList(proceedingJoinPoint.getArgs()).stream().map(s -> s.toString())
                    .collect(Collectors.joining(":"));
            Object object = redisTemplate.opsForValue().get(RedisKey);
            MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
            Class returnType = signature.getReturnType();
            if (object != null)
                return object;
            // 加分布式锁
            RLock lock = redissonClient.getLock(RedisConstant.ALBUM_LOCK_PREFIX +RedisKey  + RedisConstant.CACHE_LOCK_SUFFIX);
            try {
                lock.lock();
                // 实现二次查询
                object = redisTemplate.opsForValue().get(RedisKey);
                if (object != null) return object;
                try {
                    object = proceedingJoinPoint.proceed();
                    if (object == null) {
                        object = returnType.newInstance();
                        redisTemplate.opsForValue().set(RedisKey, object, RedisConstant.ALBUM_TEMPORARY_TIMEOUT,
                                TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(RedisKey, object);
                    }
                    return object;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } finally {
                lock.unlock();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
