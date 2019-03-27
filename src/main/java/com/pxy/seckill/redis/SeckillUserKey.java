package com.pxy.seckill.redis;

public class SeckillUserKey extends BasePrefix{
    public static final int TOKEN_EXPIRE = 3600*24*2;
    private SeckillUserKey(int expireSeconds,String prefix){
        super(expireSeconds,prefix);
    }
    public static SeckillUserKey token = new SeckillUserKey(TOKEN_EXPIRE,"tk");
    public static SeckillUserKey getById = new SeckillUserKey(0,"id");
    //为啥要将他的过期时间设置为0，那不就立即过期了吗？缓存不就没用了
}
