package com.pxy.seckill.controller;

import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.SeckillUserKey;
import com.pxy.seckill.service.impl.GoodsServiceImpl;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import com.pxy.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    SeckillUserServiceImpl userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsServiceImpl goodsService;

    @RequestMapping("/to_list")
    public String list(Model model, @CookieValue(value = "token", required = false) String token){
        if(token==null){
            return "login";
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user==null){
            return "login";
        }
        model.addAttribute("user",user);
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
        return "goods_list";
    }

    @RequestMapping("/to_detail/{goodsId}")
    public String detail(Model model, @CookieValue(value = "token", required = false) String token,
                         @PathVariable("goodsId") long goodsId){
        if(token==null){
            return "login";
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user==null){
            return "login";
        }
        model.addAttribute("user",user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus=0;
        int remainSeconds=0;
        if (now<startAt){//seckill未开始
            seckillStatus=0;
            remainSeconds=(int)((startAt-now)/1000);
        }else if(now>endAt){//seckill已结束
            seckillStatus=2;
            remainSeconds=-1;
        }else {//seckill正在进行中
            seckillStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("seckillStatus",seckillStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        return "goods_detail";
    }

}
