package com.pxy.seckill.access;

import com.pxy.seckill.entity.SeckillUser;

/**
 * 定义一个线程私有的对象userHolder，注意它是一个类变量。
 * ThreadLocal为变量在每个线程中都创建了一个副本，那么每个线程可以访问自己内部的副本变量。
 */
public class UserContext {
    private static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<>();
    public static void setUser(SeckillUser user){
        userHolder.set(user);
    }
    public static SeckillUser getUser(){
        return userHolder.get();
    }
}
