package com.pxy.seckill.service;

import com.pxy.seckill.entity.SeckillUser;
import com.pxy.seckill.vo.LoginVo;

import javax.servlet.http.HttpServletResponse;

public interface SeckillUserService {
    /**
     * 根据id获取用户信息
     * @param id
     * @return
     */
    public SeckillUser getById(long id);

    /**
     * 根据token获取用户信息
     * @param response
     * @param token
     * @return
     */
    public SeckillUser getByToken(HttpServletResponse response,String token);

    /**
     * 用户登录
     * @param response
     * @param loginVo
     * @return
     */
    public boolean login(HttpServletResponse response, LoginVo loginVo);
}
