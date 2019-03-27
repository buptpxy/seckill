package com.pxy.seckill.redis;

public class GoodsKey extends BasePrefix {
    public GoodsKey(String prefix){
        super(prefix);
    }
    public static GoodsKey getGoodsById = new GoodsKey("gId");
}
