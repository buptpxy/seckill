package com.pxy.seckill.rabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Consumer {
    public  static  void main(String[]args) throws Exception{
        //创建连接对象
        ConnectionFactory factory = new ConnectionFactory();
        //设置rabbitmq所在的ip,用户名密码
        factory.setHost("localhost");
        factory.setUsername("pxy");
        factory.setPassword("123456");
        factory.setPort(5672);
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //声明一个direct模式的交换机
        /**
         * channel.exchangeDeclare(String exchange,String type,boolean durable,boolean autodelete....)
         参数1：exchange 交换机对象的名称
         参数2 type
         BuiltinExchangeType. DIRECT("direct"),定向发送消息
         BuiltinExchangeType. FANOUT("fanout"),会向所有的queue广播所有收到的消息。如log系统可使用此模式
         BuiltinExchangeType. TOPIC("topic"),
         BuiltinExchangeType. HEADERS("headers");
         参数3：durable：如果设置了true，本交换机在重启后也会生存（实际上不一定靠谱，设置为true就好）
         参数n：。。。。。。。
         */
        channel.exchangeDeclare("exchange_name", BuiltinExchangeType.DIRECT,true);
        //声明一个非持久化自动删除的队列，如果该队列不被使用就删除它
        channel.queueDeclare("queue_name",false,false,true,null);
        //绑定到交换机，设置在交换机中的路由规则
        channel.queueBind("queue_name","exchange_name","route_key");
        //创建消费者对象
        com.rabbitmq.client.Consumer consumer=new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume("queue_name",true,consumer);
    }

}
