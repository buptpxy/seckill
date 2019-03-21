package com.pxy.seckill.redis;

public interface KeyPrefix {
    /**
     * 键的过期时间
     * @return int
     */
    public int expireSeconds();

    /**
     * 获取键的前缀
     * @return String
     */
    public String getPrefix();
}
