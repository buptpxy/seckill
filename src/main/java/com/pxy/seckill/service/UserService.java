package com.pxy.seckill.service;

import com.pxy.seckill.entity.User;


public interface UserService {
    /**
     *
     * @param id
     * @return User
     */
    User getById(int id);

    /**
     * 一个事务
     * @return boolean
     */
    boolean tx();
}
