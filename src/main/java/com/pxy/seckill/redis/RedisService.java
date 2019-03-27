package com.pxy.seckill.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {
    @Autowired
    JedisPool jedisPool;

    /**
     * 将String类型的value转为T类型的对象
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> T stringToObject(String str, Class<T> clazz) {
        if(str==null || str.length()==0 || clazz==null){
            return null;
        }
        //int.class是指int的class对象
        if (clazz==int.class || clazz==Integer.class){
            return (T)Integer.valueOf(str);
        }else if(clazz==String.class){
            return (T)str;
        }else if(clazz==long.class || clazz==Long.class){
            return (T)Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }

    /**
     * 把T类型的value对象转换为String类型
     * @param value
     * @param <T>
     * @return
     */
    private <T> String objectToString(T value){
        if (value==null){
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz==int.class || clazz==Integer.class || clazz == long.class || clazz == Long.class){
            return String.valueOf(value);
        }
        return JSON.toJSONString(value);
    }
    /**
     * 将jedis还给连接池
     * @param jedis
     */
    private void returnToPool(Jedis jedis) {
        if (jedis!=null){
            jedis.close();
        }
    }

    /**
     * prefix是干啥使的？前缀是为了区分存入的是哪个字段的值，比如有可能id和name都是1，
     * 但是存入redis中的key就是id1和name1
     * 根据key从jedis中获取value
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(KeyPrefix prefix,String key,Class<T> clazz){
        Jedis jedis = null;
        try {
//            从连接池中获取一个连接
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            String str=jedis.get(realKey);
            T t=stringToObject(str,clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 往jedis里面放入一个键值对
     * @param prefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean set(KeyPrefix prefix,String key,T value){
        Jedis jedis = null;
        try {
            jedis=jedisPool.getResource();
            String val=objectToString(value);
            if(val==null || val.length()==0){
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            //过期时间
            int timeout = prefix.expireSeconds();
            if (timeout<=0){
                jedis.set(realKey,val);
            }else {
                jedis.setex(realKey,timeout,val);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断key是否存在jedis中
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix prefix,String key){
        Jedis jedis=null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除
     * */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            long ret =  jedis.del(key);
            return ret > 0;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Redis的Incr命令将key对应的value增一，为原子操作。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，
     * 然后再执行 INCR 操作，且将key的有效时间设置为长期有效。
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis=null;
        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 将key对应的value减一
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix,String key){
        Jedis jedis=null;
        try {
            jedis=jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

}
