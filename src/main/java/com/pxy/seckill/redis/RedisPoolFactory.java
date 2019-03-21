package com.pxy.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 这里为啥要注解为Service?
 */
@Service
public class RedisPoolFactory {
    @Autowired
    RedisConfig redisConfig;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Bean
    public JedisPool JedisPoolFactory(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait()*1000);//把秒换算成毫秒传入
        JedisPool jp = new JedisPool(poolConfig,redisConfig.getHost(),
                redisConfig.getPort(),redisConfig.getTimeout()*1000,
                redisConfig.getPassword(),redisConfig.getDatabase());
        logger.info("JedisPool注入成功!");
        logger.info("redis地址：" + redisConfig.getHost() + ":" + redisConfig.getPort());
        return jp;
    }
}
