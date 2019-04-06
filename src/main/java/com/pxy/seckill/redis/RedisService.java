package com.pxy.seckill.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

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
    public static  <T> T stringToObject(String str, Class<T> clazz) {
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
    public static  <T> String objectToString(T value){
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

    public boolean delAllSamePrefix(KeyPrefix prefix) {
        if (prefix == null) {
            return false;
        }
        List<String> keys = scanKeys(prefix.getPrefix());
        if (keys == null || keys.size() <= 0) {
            return true;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //list的toArray方法是一个泛型方法，需指定转换后的参数类型
            //但是使用无参数的toArray()有一个缺点，就是转换后的数组类型是Object[]。 虽然Object数组也不是不能用，但当你真的想用一个具体类型的数组，比如String[]时，问题就来了。而把Object[]给cast成String[]还是很麻烦的,需要用到这个：
            //String[] stringArray = Arrays.copyOf(objectArray, objectArray.length, String[].class);

            //此处应转换为一个String数组，因此传入一个空的String数组（长度为0）
            jedis.del(keys.toArray(new String[0]));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 获得有相同前缀的key
     * @param prefix
     * @return 匹配的key集合
     */
    public List<String> scanKeys(String prefix){
        Jedis jedis=null;
        try {
            jedis = jedisPool.getResource();
            List<String> keys = new ArrayList<String>();
            String cursor = "0";// 开始游标
            //keys、smembers等Redis命令会一次性扫描所有记录，
            // 如果redis数据量非常大，会影响redis性能，不适合用于生产环境。
            // redis2.8版本以后有了一个新命令scan，可以用来分批次扫描redis记录，
            // 这样肯定会导致整个查询消耗的总时间变大，但不会影响redis服务卡顿，影响服务使用。
            ScanParams sp = new ScanParams();//key扫描器
            sp.match("*"+prefix+"*");//匹配规则
            sp.count(100);//一次扫描记录的个数，值越大消耗的时间越短，但会影响redis性能。建议设为一千到一万
            do {
                ScanResult<String> ret = jedis.scan(cursor,sp);
                List<String> result = ret.getResult();
                if (result!=null && result.size()>0){
                    keys.addAll(result);
                }
                //cursor表示的是扫描后的游标，从0开始遍历，到0结束遍历。
                cursor = ret.getStringCursor();
            }while (!cursor.equals("0"));
            return keys;
        }finally {
            if (jedis!=null){
                jedis.close();
            }
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
