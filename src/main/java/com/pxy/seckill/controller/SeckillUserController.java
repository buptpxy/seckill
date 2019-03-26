package com.pxy.seckill.controller;

import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.service.impl.SeckillUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class SeckillUserController {
    @Autowired
    SeckillUserServiceImpl seckillUserService;
    @RequestMapping("/insertBatch")
    @ResponseBody
    public int insertBatch(){
        return seckillUserService.insertBatch();
    }
}
