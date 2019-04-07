package com.pxy.seckill.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 定义一个用于控制用户在一定时间内访问次数的注解
 */
@Target(METHOD)//注解目标
@Retention(RUNTIME)//注解生效范围
public @interface AccessLimit {
    int seconds();
    int maxCount();
    boolean needLogin() default true;
}
