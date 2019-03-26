package com.pxy.seckill.controller;


import com.pxy.seckill.dto.Result;
import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.redis.RedisService;
import com.pxy.seckill.service.SeckillUserService;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import com.pxy.seckill.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    SeckillUserServiceImpl userService;
    @Autowired
    RedisService redisService;
    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        log.info(loginVo.toString());
        userService.login(response,loginVo);
        return Result.success(true);
    }
}
