package com.ruc.consumer;

import com.ruc.anno.RMQListener;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MyConsumer {
    @RMQListener(consumerGroup = "${spring.application.name}", topic = "TestTopic", messageType = String.class)
    public void onTestMessage(MessageExt messageExt) {
        System.out.println("onTestMessage received: " + messageExt.getMsgId() + " message body: " + new String(messageExt.getBody(), StandardCharsets.UTF_8));
        // TODO 为什么没有消费完消息？10 条只消费 6 条？
        // TODO handle message
    }

    @RMQListener(consumerGroup = "${spring.application.name}", topic = "DevTopic", messageType = String.class)
    public void onDevMessage(MessageExt messageExt) {
        System.out.println("onDevMessage received: " + messageExt.getMsgId() + " message body: " + new String(messageExt.getBody(), StandardCharsets.UTF_8));
    }
}
