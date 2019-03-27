package com.pxy.seckill.dao;

import com.pxy.seckill.entity.SeckillUser;
import org.apache.ibatis.annotations.*;

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

    @Update("update seckill_user set password = #{password} where id=#{id}")
    public void update(SeckillUser user);
}
