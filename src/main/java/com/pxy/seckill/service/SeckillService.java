package com.pxy.seckill.service;

import com.pxy.seckill.entity.Seckill;
import com.pxy.seckill.exception.SeckillCloseException;
import com.pxy.seckill.exception.SeckillException;
import com.pxy.seckill.dto.Exposer;
import com.pxy.seckill.dto.SeckillExecution;
import com.pxy.seckill.exception.RepeatKillException;

import java.math.BigDecimal;
import java.util.List;

/**
 * 业务接口：应该站在"使用者"的角度设计，比如：
 * 1.定义方法的颗粒度要细
 * 2.方法的参数要明确且简练，不建议用类似Map这种让使用者封装一堆参数传递过来
 * 3.方法的return类型，除了要明确返回值类型，还应该指定该方法可能抛出的异常
 *
 * @auther pxy
 * @date 2019/3/19
 */
public interface SeckillService {

    /**
     * 获取所有的秒杀商品列表
     *
     * @return
     */
    List<Seckill> findAll();

    /**
     * 获取某一条商品秒杀信息
     *
     * @param seckillId
     * @return
     */
    Seckill findById(long seckillId);

    /**
     * 秒杀开始时输出暴露秒杀的地址
     * 否者输出系统时间和秒杀时间
     *
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀的操作
     *
     * @param seckillId
     * @param userPhone
     * @param money
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, BigDecimal money, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;


}
