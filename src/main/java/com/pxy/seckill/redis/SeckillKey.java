package com.pxy.seckill.redis;

public class SeckillKey extends BasePrefix {
    public SeckillKey(int expireSeconds,String prefix){
        super(expireSeconds,prefix);
    }
    public final static SeckillKey isGoodsOver=new SeckillKey(60,"gOver");
}
