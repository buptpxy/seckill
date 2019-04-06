package com.pxy.seckill.service.impl;

import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.SeckillKey;
import com.pxy.seckill.service.GoodsService;
import com.pxy.seckill.service.NewSeckillService;
import com.pxy.seckill.service.OrderService;
import com.pxy.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewSeckillServiceImpl implements NewSeckillService {
    @Autowired
    GoodsServiceImpl goodsService;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo seckill0(SeckillUser user, GoodsVo goods){
        //先减库存
        goodsService.reduceStock(goods);
        //再插入订单
        return orderService.createOrder(user,goods);
    }

    @Override
    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods){
        //先减库存
       boolean success = goodsService.reduceStock(goods);
       if (success){
           //再插入订单
           return orderService.createOrder(user,goods);
       }else {
           setGoodsOver(goods.getId());
           return null;
       }

    }
    private void setGoodsOver(long goodsId){
        redisService.set(SeckillKey.isGoodsOver,""+goodsId,true);
    }
    private boolean getGoodsOver(long goodsId){
        return redisService.exists(SeckillKey.isGoodsOver,""+goodsId);
    }

    public long getSeckillResult(long userId,long goodsId){
        SeckillOrders order = orderService.getSeckillOrderByUserIdGoodsId(userId,goodsId);
        if(order!=null){//秒杀成功
            return order.getGoodsId();
        }else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver){
                return -1; //秒杀失败
            }else {
                return 0;//排队中
            }
        }
    }

    public void reset(List<GoodsVo> goodsList){
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }
}
