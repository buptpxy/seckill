package com.pxy.seckill.rabbitMQ;

import com.pxy.seckill.entity.SeckillUser;

public class SeckillMessage {
    private SeckillUser user;

    public SeckillUser getUser() {
        return user;
    }

    public void setUser(SeckillUser user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }

    private long goodsId;

}
