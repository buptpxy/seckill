package com.pxy.seckill.service.impl;

import com.pxy.seckill.dao.GoodsDao;
import com.pxy.seckill.entity.SeckillGoods;
import com.pxy.seckill.service.GoodsService;
import com.pxy.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsDao goodsDao;

    @Override
    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    @Override
    public GoodsVo getGoodsVoByGoodsId(long goodsId){
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    @Override
    @Transactional
    public void reduceStock(GoodsVo goods){
//        SeckillGoods seckillGoods=new SeckillGoods();
//        seckillGoods.setGoodsId(goods.getId());
        goodsDao.reduceSeckillStock(goods.getId());
//        goodsDao.reduceGoodsStock(goods.getId());
    }
}
