package com.pxy.seckill.service;

import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.vo.GoodsVo;

public interface OrderService {
    /**
     * 根据userId 和goodsId查询SeckillOrders
     * @param userId
     * @param goodsId
     * @return
     */
    public SeckillOrders getSeckillOrderByUserIdGoodsId(long userId,long goodsId);

    /**
     * 根据SeckillUser和GoodsVo创建新的Order
     * @param user
     * @param goods
     * @return
     */
    public OrderInfo createOrder(SeckillUser user, GoodsVo goods);

}
