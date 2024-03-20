package com.ruc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "${spring.application.name}", topic = "${rocketmq.consumer.topic}")
public class TestConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String msg) {
        log.info("TestConsumer receive message: {}", msg);
    }
}
