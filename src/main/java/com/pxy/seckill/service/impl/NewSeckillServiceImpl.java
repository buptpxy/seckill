package com.pxy.seckill.service.impl;

import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.service.GoodsService;
import com.pxy.seckill.service.NewSeckillService;
import com.pxy.seckill.service.OrderService;
import com.pxy.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewSeckillServiceImpl implements NewSeckillService {
    @Autowired
    GoodsServiceImpl goodsService;

    @Autowired
    OrderServiceImpl orderService;

    @Override
    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods){
        //先减库存
        goodsService.reduceStock(goods);
        //再插入订单
        return orderService.createOrder(user,goods);
    }
}
