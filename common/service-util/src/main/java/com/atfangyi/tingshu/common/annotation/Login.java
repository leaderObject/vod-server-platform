package com.atfangyi.tingshu.common.annotation;

import java.lang.annotation.*;

/*
 * @Author:  方毅
 * @date:  2025/10/19 19:47
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(value = ElementType.METHOD)
@Inherited
public @interface Login {

    boolean required() default false;

}
