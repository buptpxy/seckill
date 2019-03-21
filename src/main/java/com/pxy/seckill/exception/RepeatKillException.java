package com.pxy.seckill.exception;
/**
 * 重复执行秒杀的异常（运行期异常）
 *
 * @auther pxy
 * @date 2019/3/19
 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
