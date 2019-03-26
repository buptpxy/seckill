package com.pxy.seckill.service.impl;

import com.pxy.seckill.dao.OrderDao;
import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
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

    @Override
    public SeckillOrders getSeckillOrderByUserIdGoodsId(long userId,long goodsId){
        return orderDao.getSeckillOrderByUserIdGoodsId(userId,goodsId);
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
        if (affectRows==1){
            long orderId=orderInfo.getId();
            SeckillOrders seckillOrder=new SeckillOrders();
            seckillOrder.setGoodsId(goods.getId());
            seckillOrder.setOrderId(orderId);
            seckillOrder.setUserId(user.getId());
            orderDao.insertSeckillOrder(seckillOrder);
        }
        return orderInfo;
    }
}
