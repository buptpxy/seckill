package com.pxy.seckill.redis;

public class GoodsKey extends BasePrefix {
    public GoodsKey(int expireSeconds, String prefix){
        super(expireSeconds,prefix);
    }
    public static GoodsKey getGoodsById = new GoodsKey(60,"gId");
    public static GoodsKey getGoodsList = new GoodsKey(60,"glist");
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"gdtl");
    public static GoodsKey getSeckillGoodsStock = new GoodsKey(0,"gst");
}
