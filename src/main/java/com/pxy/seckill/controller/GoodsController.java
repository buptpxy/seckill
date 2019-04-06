package com.pxy.seckill.controller;

import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.redis.GoodsKey;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.SeckillUserKey;
import com.pxy.seckill.service.impl.GoodsServiceImpl;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import com.pxy.seckill.vo.GoodsDetailVo;
import com.pxy.seckill.vo.GoodsVo;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

//    @Autowired
//    ApplicationContext applicationContext;


    /**
     * 780QPS 10000 ,2811QPS 15000
     * @param model
     * @param token
     * @return
     */
    @RequestMapping("/to_list/withoutRedis")
    public String list0(Model model, @CookieValue(value = "token", required = false) String token){
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

    @RequestMapping(value="/to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response,Model model,SeckillUser user){
        model.addAttribute("user",user);
        //取缓存，注意后面的class类型，是String.class
        String html = redisService.get(GoodsKey.getGoodsList,"",String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        //缓存中没有则手动渲染
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
        //使用thymeleafViewResolver渲染模板
        WebContext ctx = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    /**
     * jemeter测压得到结果为CodeMsg.SESSION_ERROR，但是传入的token并未失效
     * @param model
     * @param token
     * @param goodsId
     * @return
     */
    @RequestMapping("/to_detail0/{goodsId}")
    @ResponseBody //忘了 @ResponseBody后出现 Error resolving template [goods/to_detail0/2]错误
    public Result<GoodsDetailVo> detail0(Model model, @CookieValue(value = "token", required = false) String token,
                                         @PathVariable("goodsId") long goodsId){
        if(token==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        if (user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
//        model.addAttribute("user",user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//        model.addAttribute("goods",goods);
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
//        model.addAttribute("seckillStatus",seckillStatus);
//        model.addAttribute("remainSeconds",remainSeconds);
        GoodsDetailVo goodsDetail=new GoodsDetailVo();
        goodsDetail.setGoods(goods);
        goodsDetail.setRemainSeconds(remainSeconds);
        goodsDetail.setSeckillStatus(seckillStatus);
        goodsDetail.setUser(user);
        return Result.success(goodsDetail);
    }


    @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String detail(HttpServletRequest request,HttpServletResponse response,Model model,
                         @CookieValue(value = "token", required = false) String token,
                         @PathVariable("goodsId") long goodsId){
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }

        //缓存中没有则手动渲染
        if(token==null){
            return "login";//应该返回的是login页面，而不是"login"字符串
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
        WebContext ctx = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
//        return "goods_detail";
    }

}
