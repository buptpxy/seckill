package com.pxy.seckill.controller;

import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.Goods;
import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.SeckillUserKey;
import com.pxy.seckill.service.GoodsService;
import com.pxy.seckill.service.impl.GoodsServiceImpl;
import com.pxy.seckill.service.impl.NewSeckillServiceImpl;
import com.pxy.seckill.service.impl.OrderServiceImpl;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import com.pxy.seckill.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seckill")
public class NewSeckillController {
    @Autowired
    SeckillUserServiceImpl userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsServiceImpl goodsService;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    NewSeckillServiceImpl seckillService;

    @RequestMapping("/do_seckill0")
    public String list(Model model, @CookieValue(value = "token", required = false) String token,
                       @RequestParam("goodsId")long goodsId){
        if(token==null){
            return "login";
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user==null){
            return "login";
        }
        model.addAttribute("user",user);
        //判断库存
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        int stock=goods.getStockCount();
        if(stock<=0){
            model.addAttribute("errmsg", CodeMsg.SEC_KILL_OVER.getMsg());
            return "seckill_fail";
        }
        //判断是否重复秒杀
        SeckillOrders order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            model.addAttribute("errmsg",CodeMsg.REPEATE_SEC_KILL.getMsg());
            return "seckill_fail";
        }
        //减库存、下订单、写入秒杀订单
        OrderInfo orderInfo = seckillService.seckill(user,goods);
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goods);
        return "order_detail";
    }

    @RequestMapping(value = "/do_seckill",method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> seckill(Model model,
                                     @CookieValue(value = "token", required = false) String token,
                                    @RequestParam("goodsId")long goodsId ){
        if(token==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",user);
//        if (user==null){
//            return Result.error(CodeMsg.SESSION_ERROR);
//        }
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock<=0){
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //判断是否已经秒杀到了
        SeckillOrders order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){
            return Result.error(CodeMsg.REPEATE_SEC_KILL);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = seckillService.seckill(user,goods);
        return Result.success(orderInfo);
    }
}
