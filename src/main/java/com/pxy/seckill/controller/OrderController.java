package com.pxy.seckill.controller;

import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.service.impl.GoodsServiceImpl;
import com.pxy.seckill.service.impl.OrderServiceImpl;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import com.pxy.seckill.vo.GoodsVo;
import com.pxy.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    SeckillUserServiceImpl userService;

    @Autowired
    RedisService redisService;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    GoodsServiceImpl goodsService;

    /**
     * 直接连接数据库 QPS：654/s 出现异常：Data source rejected establishment of connection,  message from server: "Too many connections"
     * 使用redis后 QPS：3181/s 5000*2时正常 5000*3时开始出现异常，
     * 因为linux分配给客户端的连接端口用尽 无法建立socket连接所致，虽然socket正常关闭，
     * 但是端口不是立即释放，而是处于TIME_WAIT状态， 默认等待60s后才释放。
     * 通过netstat -nat|grep 8080 发现连接这个端口的线程都处于time_wait状态
     * 查看linux支持的客户端连接端口范围
     * cat /proc/sys/net/ipv4/ip_local_port_range
     * 32768 - 61000, 也就是28232个端口。
     * @param model
     * @param
     * @param orderId
     * @return
     */
    @RequestMapping("/detailForJmeterTest")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model,
                                      @RequestParam("orderId") long orderId){

        OrderInfo order = orderService.getOrderInfoById(orderId);
        if (order==null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId=order.getGoodsId();
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo orderDetail = new OrderDetailVo();
        orderDetail.setGoods(goods);
        orderDetail.setOrder(order);
        return Result.success(orderDetail);
    }

    @RequestMapping("/to_detail/{orderId}")
    @ResponseBody
    public Result<OrderDetailVo> info1(Model model, SeckillUser user,
                                       @PathVariable("orderId") long orderId){
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderInfoById(orderId);
        if (order==null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId=order.getGoodsId();
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo orderDetail = new OrderDetailVo();
        orderDetail.setGoods(goods);
        orderDetail.setOrder(order);
        return Result.success(orderDetail);
    }
}
