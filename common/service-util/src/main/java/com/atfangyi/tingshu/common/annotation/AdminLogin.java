package com.atfangyi.tingshu.common.annotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/*
 * @Author:  方毅
 * @date:  2025/11/14 22:58
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface AdminLogin {

    String prefix() default "adminInfo:";

    String suffix() default "suffix";
}
