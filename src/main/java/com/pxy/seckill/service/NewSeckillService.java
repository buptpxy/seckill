package com.pxy.seckill.service;

import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.vo.GoodsVo;
import org.springframework.stereotype.Service;

@Service
public interface NewSeckillService {
    /**
     * 执行秒杀
     * @param user
     * @param goods
     * @return
     */
    public OrderInfo seckill(SeckillUser user, GoodsVo goods);
}
