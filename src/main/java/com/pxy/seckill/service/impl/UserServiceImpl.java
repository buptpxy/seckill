package com.pxy.seckill.service.impl;

import com.pxy.seckill.entity.User;
import com.pxy.seckill.mapper.UserMapper;
import com.pxy.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    public User getById(int id){
        return userMapper.getById(id);
    }

    @Transactional
    public boolean tx(){
        User u1=new User();
//        u1.setId(5);
        u1.setName("pxy2");
        userMapper.insert(u1);

        User u2=new User();
//        u2.setId(3);
        u2.setName("pxy4");
        userMapper.insert(u2);

        return true;
    }
}
