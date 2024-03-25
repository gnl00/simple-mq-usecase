package com.ruc.consumer;

import com.ruc.anno.RMQListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class MultiListenerConsumer {
    @RMQListener(consumerGroup = "${rmq-uc.consumer.group.test}", topic = "${rmq-uc.topic.test}", messageType = String.class)
    public void onTestMessage(MessageExt messageExt) {
        System.out.println("MultiListenerConsumer#onTestMessage received: " + messageExt.getMsgId() + " message body: " + new String(messageExt.getBody(), StandardCharsets.UTF_8));
    }

    @RMQListener(consumerGroup = "${rmq-uc.consumer.group.dev}", topic = "${rmq-uc.topic.dev}", messageType = String.class)
    public void onDevMessage(MessageExt messageExt) {
        System.out.println("MultiListenerConsumer#onDevMessage received: " + messageExt.getMsgId() + " message body: " + new String(messageExt.getBody(), StandardCharsets.UTF_8));
    }
}
