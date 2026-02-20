package com.atfangyi.tingshu.common.annotation;

import java.lang.annotation.*;

/*
 * @Author:  方毅
 * @date:  2025/11/2 17:55
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(value = ElementType.METHOD)
@Inherited
public @interface Cache {

    String prefix() default "data:";


    String suffix() default ":cache";
}
