package com.pxy.seckill.exception;

/**
 * 秒杀关闭异常
 * @auther pxy
 * @date 2019/3/19
 */
public class SeckillCloseException extends SeckillException {

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}