package com.pxy.seckill.redis;

public class SeckillKey extends BasePrefix {
    public SeckillKey(int expireSeconds,String prefix){
        super(expireSeconds,prefix);
    }
    public final static SeckillKey isGoodsOver=new SeckillKey(300,"gOver");
    public final static SeckillKey getSeckillPath = new SeckillKey(300,"spath");
    public final static SeckillKey getSeckillVerifyCode=new SeckillKey(300,"svcode");
}
