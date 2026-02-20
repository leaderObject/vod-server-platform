package com.atfangyi.tingshu.user.annotation;


import java.lang.annotation.*;

/**
 * @className: SendCode
 * @author: 方毅
 * @date: 2025/12/10 19:07
 * @version: 1.0
 * @description: TODO
 */

@SuppressWarnings({"all"})
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SendCode {
    String prefix() default "prefix:";

    String suffix() default "count";
}
