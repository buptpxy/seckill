package com.pxy.seckill.mapper;

import com.pxy.seckill.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    /**
     *
     * @param id
     * @return User
     */
    User getById(int id);

    /**
     *
     * @param user
     * @return int
     */
    int insert(User user);
}
