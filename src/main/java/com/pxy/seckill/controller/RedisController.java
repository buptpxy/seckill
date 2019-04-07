package com.pxy.seckill.controller;

import com.pxy.seckill.dto.Result;
import com.pxy.seckill.redis.GoodsKey;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/redis")
public class RedisController {
    @Autowired
    RedisService redisService;

    @RequestMapping("/testValue")
    @ResponseBody
    public Result<String> test(){
        redisService.set(UserKey.getById,"testNullValue",null);
        String res = redisService.get(UserKey.getById,"testNullValue",String.class);
        return Result.success(res);
        //结果为{"code":0,"msg":"success","data":null}，redis中并为查到key为此键的键值对，说明set的value为null时，redis直接不会将其存入
    }
}
