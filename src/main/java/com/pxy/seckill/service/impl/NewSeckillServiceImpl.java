package com.pxy.seckill.service.impl;

import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.SeckillKey;
import com.pxy.seckill.service.GoodsService;
import com.pxy.seckill.service.NewSeckillService;
import com.pxy.seckill.service.OrderService;
import com.pxy.seckill.util.MD5Util;
import com.pxy.seckill.util.UUIDUtil;
import com.pxy.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class NewSeckillServiceImpl implements NewSeckillService {
    @Autowired
    GoodsServiceImpl goodsService;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    RedisService redisService;

    public String createSeckillPath(SeckillUser user,long goodsId){
        if (user==null || goodsId<=0){
            return null;
        }
        //如果已经为同一个用户和商品创建path了，就不再次创建了
        String str = redisService.get(SeckillKey.getSeckillPath,""+user.getId()+"_"+goodsId,String.class);
        if (str==null){
            str = MD5Util.md5(UUIDUtil.uuid()+"123456");
            redisService.set(SeckillKey.getSeckillPath,""+user.getId()+"_"+goodsId,str);
        }
        //路径为随机值加盐加密
        return str;
    }

    public boolean checkPath(SeckillUser user,long goodsId,String path){
        if (user==null || path==null){
            return false;
        }
        String pathOld = redisService.get(SeckillKey.getSeckillPath,""+user.getId()+"_"+goodsId,String.class);
        return path.equals(pathOld);
    }

    @Override
    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods){
        //可更改成先插入订单再减库存，因为insert不会获取行锁，而update会
        //先减库存
       boolean success = goodsService.reduceStock(goods);
       if (success){
           //再插入订单
           return orderService.createOrder(user,goods);
       }else {
           setGoodsOver(goods.getId());
           return null;
       }

    }
    private void setGoodsOver(long goodsId){
        redisService.set(SeckillKey.isGoodsOver,""+goodsId,true);
    }
    private boolean getGoodsOver(long goodsId){
        return redisService.exists(SeckillKey.isGoodsOver,""+goodsId);
    }

    public long getSeckillResult(long userId,long goodsId){
        SeckillOrders order = orderService.getSeckillOrderByUserIdGoodsId(userId,goodsId);
        if(order!=null){//通过订单是否存在判断消费者是否消费成功
            return order.getOrderId();
        }else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver){
                return -1; //秒杀失败
            }else {
                return 0;//排队中
            }
        }
    }

    public void reset(List<GoodsVo> goodsList){
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }
//    @Transactional
//    public OrderInfo seckill0(SeckillUser user, GoodsVo goods){
//        //先减库存
//        goodsService.reduceStock(goods);
//        //再插入订单
//        return orderService.createOrder(user,goods);
//    }
//     public BufferedImage createVerifyCode(SeckillUser user,long goodsId){
//        if (user==null || goodsId<=0){
//            return null;
//        }
//        int width=80;
//        int height=32;
//        //create the image
//        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
//        Graphics g = image.getGraphics();
//        // set the background color
//        g.setColor(new Color(0xDCDCDC));
//        g.fillRect(0, 0, width, height);
//        // draw the border
//        g.setColor(Color.black);
//        g.drawRect(0, 0, width - 1, height - 1);
//        // create a random instance to generate the codes
//        Random rdm = new Random();
//        // make some confusion
//        for (int i = 0; i < 50; i++) {
//            int x = rdm.nextInt(width);
//            int y = rdm.nextInt(height);
//            g.drawOval(x, y, 0, 0);
//        }
//        // generate a random code
//        String verifyCode = generateVerifyCode(rdm);
//        g.setColor(new Color(0, 100, 0));
//        g.setFont(new Font("Candara", Font.BOLD, 24));
//        g.drawString(verifyCode, 8, 24);
//        g.dispose();
//        //把验证码存到redis中
//        int rnd = calc(verifyCode);
//        redisService.set(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId, rnd);
//        //输出图片
//        return image;
//    }
//    public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
//        if(user == null || goodsId <=0) {
//            return false;
//        }
//        Integer codeOld = redisService.get(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId, Integer.class);
//        if(codeOld == null || codeOld - verifyCode != 0 ) {
//            return false;
//        }
//        redisService.delete(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId);
//        return true;
//    }
//    private static int calc(String exp) {
//        try {
//            ScriptEngineManager manager = new ScriptEngineManager();
//            ScriptEngine engine = manager.getEngineByName("JavaScript");
//            return (Integer)engine.eval(exp);
//        }catch(Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//    private static char[] ops = new char[] {'+', '-', '*'};
//    /**
//     * + - *
//     * */
//    private String generateVerifyCode(Random rdm) {
//        int num1 = rdm.nextInt(10);
//        int num2 = rdm.nextInt(10);
//        int num3 = rdm.nextInt(10);
//        char op1 = ops[rdm.nextInt(3)];
//        char op2 = ops[rdm.nextInt(3)];
//        String exp = ""+ num1 + op1 + num2 + op2 + num3;
//        return exp;
//    }
}
