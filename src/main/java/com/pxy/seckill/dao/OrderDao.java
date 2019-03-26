package com.pxy.seckill.dao;

import com.pxy.seckill.entity.OrderInfo;
import com.pxy.seckill.entity.SeckillOrders;
import com.pxy.seckill.entity.SeckillUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

@Mapper
public interface OrderDao {
    /**
     * 查询订单
     * @param userId
     * @param goodsId
     * @return
     */
    @Select("select * from seckill_orders where user_id=#{userId} and goods_id=#{goodsId}")
    public SeckillOrders getSeckillOrderByUserIdGoodsId(long userId,long goodsId);

    /**
     * 插入一条订单详情
     * @SelectKey
     * 将select last_insert_id()的值，放到keyColumn中,before为true表示select在insert之前，这样不会返回自增的主键值
     * before为false表示select在insert之后，这样可以返回自增后的主键值。事实上总是返回影响的行数，所以总是得到1
     * @param orderInfo
     * @return insert后影响的行数
     */
    @Insert("insert into order_info(user_id,goods_id,goods_name,goods_count,goods_price, order_channel, status, create_date)values("+
            "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    @SelectKey(keyColumn="id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")
    public int insert(OrderInfo orderInfo);

    /**
     * 插入一条订单
     * @param seckillOrders
     * @return
     */
    @Insert("insert into seckill_orders(user_id,goods_id,order_id)values(#{userId},#{goodsId},#{orderId})")
    public int insertSeckillOrder(SeckillOrders seckillOrders);
}
