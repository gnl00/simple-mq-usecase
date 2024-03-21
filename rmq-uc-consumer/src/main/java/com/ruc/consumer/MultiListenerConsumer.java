package com.ruc.consumer;

import com.ruc.anno.RMQListener;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MultiListenerConsumer {
    @RMQListener(consumerGroup = "${rmq-uc.consumer.group.test}", topic = "TestTopic", messageType = String.class)
    public void onTestMessage(MessageExt messageExt) {
        System.out.println("MultiListenerConsumer#onTestMessage received: " + messageExt.getMsgId() + " message body: " + new String(messageExt.getBody(), StandardCharsets.UTF_8));
    }

    @RMQListener(consumerGroup = "${rmq-uc.consumer.group.dev}", topic = "DevTopic", messageType = String.class)
    public void onDevMessage(MessageExt messageExt) {
        System.out.println("MultiListenerConsumer#onDevMessage received: " + messageExt.getMsgId() + " message body: " + new String(messageExt.getBody(), StandardCharsets.UTF_8));
    }
}
