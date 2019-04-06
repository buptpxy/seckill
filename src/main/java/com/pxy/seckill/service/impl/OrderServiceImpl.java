package com.pxy.seckill.service.impl;

import com.pxy.seckill.dao.OrderDao;
import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.redis.OrderKey;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.service.OrderService;
import com.pxy.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    @Override
    public SeckillOrders getSeckillOrderByUserIdGoodsId(long userId,long goodsId){
//        return orderDao.getSeckillOrderByUserIdGoodsId(userId,goodsId);
        SeckillOrders seckillOrder = redisService.get(OrderKey.getSeckillOrderByUidGid,""+userId+"_"+goodsId,SeckillOrders.class);
        if (seckillOrder==null){
            seckillOrder = orderDao.getSeckillOrderByUserIdGoodsId(userId,goodsId);
            redisService.set(OrderKey.getSeckillOrderByUidGid,""+userId+"_"+goodsId,seckillOrder);
        }
        return seckillOrder;
    }

    public OrderInfo getOrderInfoById(long orderId){
//        return orderDao.getOrderInfoById(orderId);
        OrderInfo orderInfo = redisService.get(OrderKey.getOrderInfoById,""+orderId,OrderInfo.class);
        if (orderInfo==null){
            orderInfo = orderDao.getOrderInfoById(orderId);
            redisService.set(OrderKey.getOrderInfoById,""+orderId,orderInfo);
        }
        return orderInfo;
    }

    @Override
    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goods){
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        int affectRows = orderDao.insert(orderInfo);

        long orderId=orderInfo.getId();
        SeckillOrders seckillOrder=new SeckillOrders();
        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setOrderId(orderId);
        seckillOrder.setUserId(user.getId());
        orderDao.insertSeckillOrder(seckillOrder);

        redisService.set(OrderKey.getSeckillOrderByUidGid,""+user.getId()+"_"+goods.getId(),seckillOrder);
        return orderInfo;
    }

    public void deleteOrders(){
        orderDao.deleteAllSeckillOrders();
        orderDao.deleteAllOrderInfo();
    }
}
