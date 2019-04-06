package com.pxy.seckill.controller;

import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.Goods;
import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.rabbitMQ.MQReceiver;
import com.pxy.seckill.rabbitMQ.MQSender;
import com.pxy.seckill.rabbitMQ.SeckillMessage;
import com.pxy.seckill.redis.*;
import com.pxy.seckill.service.GoodsService;
import com.pxy.seckill.service.impl.GoodsServiceImpl;
import com.pxy.seckill.service.impl.NewSeckillServiceImpl;
import com.pxy.seckill.service.impl.OrderServiceImpl;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import com.pxy.seckill.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

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

    @Autowired
    MQSender sender;

//    @Autowired
//    MQReceiver receiver;

    private HashMap<Long,Boolean> localOverMap = new HashMap<Long,Boolean>();

    /**
     * 系统初始化，把每件商品的库存数先存入redis中
     * localOverMap干啥的？记录每个商品是否已经卖完
     * @throws Exception
     */
    public void afterPropertiesSet(){
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList==null){
            return;
        }
        for(GoodsVo goods:goodsList){
            redisService.set(GoodsKey.getSeckillGoodsStock,""+goods.getId(),goods.getStockCount());
            if(goods.getStockCount()!=0){
                localOverMap.put(goods.getId(),false);
            }else {
                localOverMap.put(goods.getId(),true);
            }

        }
    }

    /**
     * 复原
     * 为什么这里使用的是GET不是POST???
     * @return
     */
    @RequestMapping(value = "/reset",method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(){
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods:goodsList){
            goods.setStockCount(3);
            redisService.set(GoodsKey.getSeckillGoodsStock,""+goods.getId(),3);
            localOverMap.put(goods.getId(),false);
        }
        redisService.delAllSamePrefix(OrderKey.getSeckillOrderByUidGid);
        redisService.delAllSamePrefix(SeckillKey.isGoodsOver);
        seckillService.reset(goodsList);
        return Result.success(true);

    }

    /**
     *
     * @param model
     * @param token
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_seckill",method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> seckill(Model model,
                                     @CookieValue(value = "token", required = false) String token,
                                     @RequestParam("goodsId")long goodsId ){
        afterPropertiesSet();//系统初始化
        if(token==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",user);
        //内存标记商品是否卖完，卖完就直接不访问redis了
        boolean over=localOverMap.get(goodsId);
        if(over){
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //预减库存,先把redis中的库存减掉，不会出现万一很多个线程一起减，减为负数了大家都秒不了了吗
        //但如果不在redis中预减，肯定会出现超卖
        long stock=redisService.decr(GoodsKey.getSeckillGoodsStock,""+goodsId);
        //预减完如果库存为负数就把卖完标志置true
        if (stock<0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //判断是否已经秒杀到了
        SeckillOrders order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){
            return Result.error(CodeMsg.REPEATE_SEC_KILL);
        }
        //入队
        SeckillMessage sm = new SeckillMessage();
        sm.setUser(user);
        sm.setGoodsId(goodsId);
        sender.sendSeckillMessage(sm);
//        receiver.receive(sm);
// receiver无需显式调用，在MQReceiver的receive方法上注解@RabbitListener(queue="...")即可
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderInfo orderInfo = seckillService.seckill(user,goods);
        return Result.success(orderInfo);
    }

    /**
     *
     * @param model
     * @param token
     * @param goodsId
     * @return orderId:成功 -1:失败 0:排队中
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model,@CookieValue(value = "token", required = false) String token,
                                      @RequestParam("goodsId") long goodsId){
        if(token==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",user);
//        if(user==null){
//            return Result.error(CodeMsg.SESSION_ERROR);
//        }
        long result = seckillService.getSeckillResult(user.getId(),goodsId);
        return Result.success(result);
    }

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

    /**
     * jMeter测压返回session不存在或已失效
     * @param model
     * @param token
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_seckill1",method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> seckill1(Model model,
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
