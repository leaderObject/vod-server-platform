package com.atfangyi.tingshu.common.annotation;

import java.lang.annotation.*;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperatorLogAnnotation {
    public String operatorType() default "";
    public String operatorMethod() default "";
}
