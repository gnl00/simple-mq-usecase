package com.ruc;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class RMQTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rmq-uc.topic}")
    private String testTopic;

    @Test
    public void test_topic_send() {
        // rocketMQTemplate.convertAndSend(topic, "hello~");
        for (int i = 0; i < 10; i++) {
            long timestamp = System.currentTimeMillis();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String payload = timestamp + "-test-:=";
            SendResult sendResult = rocketMQTemplate.syncSend(testTopic, payload.getBytes(StandardCharsets.UTF_8));
            log.info("message: {} send result: {}", sendResult.getMsgId(), sendResult.getSendStatus().toString());
        }
    }

    @Test
    public void dev_topic_send() {
        for (int i = 0; i < 10; i++) {
            long timestamp = System.currentTimeMillis();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String payload = timestamp + "-dev-:=";
            SendResult sendResult = rocketMQTemplate.syncSend("DevTopic", payload.getBytes(StandardCharsets.UTF_8));
            log.info("message: {} send result: {}", sendResult.getMsgId(), sendResult.getSendStatus().toString());
        }
    }

}
