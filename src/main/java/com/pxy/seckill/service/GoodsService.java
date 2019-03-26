package com.pxy.seckill.service;

import com.pxy.seckill.vo.GoodsVo;

import java.util.List;

public interface GoodsService {
    public List<GoodsVo> listGoodsVo();
    public GoodsVo getGoodsVoByGoodsId(long goodsId);
    public void reduceStock(GoodsVo goods);
}
