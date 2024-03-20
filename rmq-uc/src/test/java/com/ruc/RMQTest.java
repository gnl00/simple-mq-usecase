package com.ruc;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootTest
public class RMQTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rmq-uc.topic}")
    private String topic;

    @Test
    public void mq_send() {
        // rocketMQTemplate.convertAndSend(topic, "hello~");
        for (int i = 0; i < 10; i++) {
            SendResult sendResult = rocketMQTemplate.syncSend(topic, "world".getBytes(StandardCharsets.UTF_8));
            log.info("message: {} send result: {}", sendResult.getMsgId(), sendResult.getSendStatus().toString());
        }
    }

}
