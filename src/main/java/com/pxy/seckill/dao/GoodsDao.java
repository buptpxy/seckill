package com.pxy.seckill.dao;

import com.pxy.seckill.entity.SeckillGoods;
import com.pxy.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
@Mapper
public interface GoodsDao {
    /**
     * 查询商品列表
     * @return
     */
    @Select("select g.*,sg.stock_count,sg.start_date,sg.end_date,sg.seckill_price from seckill_goods sg left join goods g on sg.goods_id=g.id")
    public List<GoodsVo> listGoodsVo();

    /**
     * 根据goodsId查询goodVo
     * @param goodsId
     * @return
     */
    @Select("select g.*,sg.stock_count,sg.start_date,sg.end_date,sg.seckill_price from seckill_goods sg left join goods g on sg.goods_id=g.id where g.id=#{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    /**
     * 减库存
     * @param
     * @return
     */
    @Update("update seckill_goods set stock_count = stock_count - 1 where goods_id=#{goodsId}")
    public int reduceSeckillStock(long goodsId);

    /**
     * 让秒杀商品的库存作为额外库存，不影响goods表的库存，减少一次数据库操作
     * @param goodsId
     * @return
     */
    @Update("update goods set goods_stock = goods_stock - 1 where id=#{goodsId}")
    public int reduceGoodsStock(long goodsId);
}
