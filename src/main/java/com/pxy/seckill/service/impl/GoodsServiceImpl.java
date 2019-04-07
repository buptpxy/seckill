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
//        List<GoodsVo> goodsList = redisService.get(GoodsKey.getGoodsList,"",List.class);
//        if (goodsList==null){
//            goodsList = goodsDao.listGoodsVo();
//            redisService.set(GoodsKey.getGoodsList,"",goodsList);
//        }
        return goodsDao.listGoodsVo();
    }

    @Override
    public GoodsVo getGoodsVoByGoodsId(long goodsId){
        GoodsVo goods = redisService.get(GoodsKey.getGoodsById,""+goodsId,GoodsVo.class);
        if (goods==null){
            goods=goodsDao.getGoodsVoByGoodsId(goodsId);
            redisService.set(GoodsKey.getGoodsById,""+goodsId, goods);
        }
        return goods;
    }

    public Integer getSeckillGoodsStock(long goodsId){
        Integer stock = redisService.get(GoodsKey.getSeckillGoodsStock,""+goodsId,Integer.class);
        if (stock==null){
            GoodsVo goods = getGoodsVoByGoodsId(goodsId);
            stock=goods.getStockCount();
            redisService.set(GoodsKey.getSeckillGoodsStock,""+goodsId,stock);
        }
        return stock;
    }

    @Override
    @Transactional
    public boolean reduceStock(GoodsVo goods){
//判断数据库减库存是否成功,此处不用让getSeckillGoodsStock缓存删除，因为缓存已经预减过了
        int res=goodsDao.reduceSeckillStock(goods.getId());
        //但是要删除getGoodsById缓存
        redisService.delete(GoodsKey.getGoodsById,""+goods.getId());
        return res>0;
    }
    public void reduceStockInRedis(long userId,long goodsId){
        //首先判断如果这个用户已经在redis中减过此商品库存了，就不为他减了
        if (redisService.exists(GoodsKey.reduceStockByUIDGID,""+userId+"_"+goodsId)){
            return;
        }
        //否则就标记他减过了
        redisService.set(GoodsKey.reduceStockByUIDGID,""+userId+"_"+goodsId,true);
        //在redis中减库存
        redisService.decr(GoodsKey.getSeckillGoodsStock,""+goodsId);
        //同时删除redis中缓存的此商品的信息
        redisService.delete(GoodsKey.getGoodsById,""+goodsId);
    }
    public void resetStock(List<GoodsVo> goodsList){
        for(GoodsVo goods : goodsList){
            SeckillGoods g = new SeckillGoods();
            g.setGoodsId(goods.getId());
            goodsDao.resetStock(g);
        }
    }

////    @Override
//    @Transactional
//    public void reduceStock0(GoodsVo goods){
////        SeckillGoods seckillGoods=new SeckillGoods();
////        seckillGoods.setGoodsId(goods.getId());
//        //reduceSeckillStock执行完毕后会改变goods对象的stock吗
//        goodsDao.reduceSeckillStock(goods.getId());
//        redisService.delete(GoodsKey.getGoodsById,""+goods.getId());
//    }
}
