package com.pxy.seckill.dao;

import com.pxy.seckill.entity.SeckillUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface SeckillUserDao {
    @Select("select * from seckill_user where id=#{id}")
    public SeckillUser getById(@Param("id")long id);

    @Insert({
            "<script>",
            "insert into seckill_user(id,nickname,password,salt,last_login_date) values",
            "<foreach collection='list' item='user' index='index' separator=','>",
            "(#{user.id},#{user.nickname},#{user.password},#{user.salt},#{user.lastLoginDate})",
            "</foreach>",
            "</script>"
    })
    public int insertUserBatch(List<SeckillUser> list);
}
