package com.pxy.seckill.controller;

import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.User;
import com.pxy.seckill.exception.CodeMsg;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.redis.UserKey;
import com.pxy.seckill.service.UserService;
import com.pxy.seckill.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @RequestMapping("/")
    @ResponseBody
    String home(){
        return "hello world!";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello(){
        return Result.success("hello,pxy");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError(){
        return Result.error(CodeMsg.SERVER_ERROR);

    }

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model){
        model.addAttribute("name","pxy");
        return "hello";//hello 是html文件的名字
    }

    @Autowired
    UserServiceImpl userService;

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user= userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx(){
        return Result.success(userService.tx());
    }

    @Autowired
    RedisService redisService;

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = redisService.get(UserKey.getById,String.valueOf(1),User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("ppp");
        redisService.set(UserKey.getById,""+1,user);
        return Result.success(true);
    }
}
