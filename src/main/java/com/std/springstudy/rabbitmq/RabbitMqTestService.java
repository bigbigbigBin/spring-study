package com.std.springstudy.rabbitmq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.UnsupportedEncodingException;

public class RabbitMqTestService {



    @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "${队列名}", durable = "true", ignoreDeclarationExceptions = "true"),
                exchange = @Exchange(value = "${交换器名}", type = ExchangeTypes.FANOUT),
                ignoreDeclarationExceptions = "true"),
            containerFactory = "stdListenerContainerFactory"  // 指明是那个集群
    )
    public void loanResultNotice(Message messageExt, Channel channel) {
        try {
            String message = new String(messageExt.getBody(),"utf-8");
            System.out.println("====== 测试数据，消费到的消息");
        } catch (UnsupportedEncodingException e) {
            System.out.println("====== 测试数据，格式转换出错");
        }

    }

}
