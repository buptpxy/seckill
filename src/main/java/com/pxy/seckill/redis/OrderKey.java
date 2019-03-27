package com.pxy.seckill.redis;

public class OrderKey extends BasePrefix{
    public OrderKey(String prefix){
        super(prefix);
    }
    //为啥不设过期时间？
    public static OrderKey getSeckillOrderByUidGid = new OrderKey("odrUG");
    public static OrderKey getOrderInfoById = new OrderKey("odrId");

}
