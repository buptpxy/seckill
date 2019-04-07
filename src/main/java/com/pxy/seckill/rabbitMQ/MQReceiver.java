package com.pxy.seckill.rabbitMQ;

import com.pxy.seckill.entity.Goods;
import com.pxy.seckill.entity.Seckill;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.service.GoodsService;
import com.pxy.seckill.service.NewSeckillService;
import com.pxy.seckill.service.OrderService;
import com.pxy.seckill.service.SeckillService;
import com.pxy.seckill.service.impl.GoodsServiceImpl;
import com.pxy.seckill.service.impl.NewSeckillServiceImpl;
import com.pxy.seckill.service.impl.OrderServiceImpl;
import com.pxy.seckill.service.impl.SeckillServiceImpl;
import com.pxy.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
    @Autowired
    RedisService redisService;

    @Autowired
    GoodsServiceImpl goodsService;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    NewSeckillServiceImpl seckillService;

    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void receive(String message){//消费者从队列中接收到的是一个String
        log.info("receive message:"+message);
        SeckillMessage seckillMessage = RedisService.stringToObject(message,SeckillMessage.class);
        SeckillUser user = seckillMessage.getUser();
        long goodsId = seckillMessage.getGoodsId();
        //消费者也会先查询stock是否>0
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock = goods.getStockCount();
//        if (stock<=0){
//            return;
//        }
//        //判断是否已经秒杀过了，避免重复秒杀
//        SeckillOrders order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(),goodsId);
//        if (order!=null){
//            return;
//        }
        //减库存 下订单 写入秒杀订单
        seckillService.seckill(user,goods);
        //消费者消费完了之后怎么发出通知？通过查询订单状态
    }
}
