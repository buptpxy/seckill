package com.pxy.seckill.vo;

import com.pxy.seckill.entity.SeckillUser;

public class GoodsDetailVo {
    private int seckillStatus=0;
    private int remainSeconds=0;
    private GoodsVo goods;
    private SeckillUser usr;

    public int getSeckillStatus() {
        return seckillStatus;
    }

    public void setSeckillStatus(int seckillStatus) {
        this.seckillStatus = seckillStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public SeckillUser getUsr() {
        return usr;
    }

    public void setUsr(SeckillUser usr) {
        this.usr = usr;
    }
}
