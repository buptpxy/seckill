package com.pxy.seckill.service.impl;

import com.pxy.seckill.dao.GoodsDao;
import com.pxy.seckill.entity.Goods;
import com.pxy.seckill.entity.SeckillGoods;
import com.pxy.seckill.redis.GoodsKey;
import com.pxy.seckill.redis.RedisService;
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

    @Autowired
    RedisService redisService;

    @Override
    public List<GoodsVo> listGoodsVo(){
//        List<GoodsVo> goodsList = redisService.get(GoodsKey.goodsList,"",List<GoodsVo>.getClass());
        return goodsDao.listGoodsVo();
    }

    @Override
    public GoodsVo getGoodsVoByGoodsId(long goodsId){
//        return goodsDao.getGoodsVoByGoodsId(goodsId);
        GoodsVo goods = redisService.get(GoodsKey.getGoodsById,""+goodsId,GoodsVo.class);
        if (goods==null){
            goods=goodsDao.getGoodsVoByGoodsId(goodsId);
            redisService.set(GoodsKey.getGoodsById,""+goodsId,goods);
        }
        return goods;
    }

    @Override
    @Transactional
    public void reduceStock(GoodsVo goods){
//        SeckillGoods seckillGoods=new SeckillGoods();
//        seckillGoods.setGoodsId(goods.getId());
        //reduceSeckillStock执行完毕后会改变goods对象的stock吗
        goodsDao.reduceSeckillStock(goods.getId());
        redisService.delete(GoodsKey.getGoodsById,""+goods.getId());
    }
}
