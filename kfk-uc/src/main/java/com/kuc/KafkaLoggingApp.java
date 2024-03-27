package com.kuc;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootApplication
public class KafkaLoggingApp {
    private static final AtomicInteger counter = new AtomicInteger(0);
    public static void main(String[] args) {
        ConfigurableApplicationContext ac = SpringApplication.run(KafkaLoggingApp.class, args);

        // doSend(ac);
    }

    // 消息生产测试
    public static void doSend(ConfigurableApplicationContext ac) {
        RocketMQTemplate rmqTemplate = ac.getBean(RocketMQTemplate.class);
        KafkaTemplate<String, String> kfkTemplate = ac.getBean(KafkaTemplate.class);
        System.out.println("start ==>");
        long sendStart = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            // rmqTemplate.convertAndSend("prometheus-metric", "msg-" + 100000); // 118s
            System.out.println("message ==> " + i);
            kfkTemplate.send("prometheus-metric-copy", "msg-round-2-" + i); // 1s
        }
        System.out.println("<== end");
        System.out.println((System.currentTimeMillis() - sendStart) / 1000);
    }

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static long start = 0;

    // Uncomment the line below to consume message
    // @KafkaListener(id = "${spring.kafka.consumer.client-id}", groupId = "${spring.kafka.consumer.group-id}", topics = {"prometheus-metric"})
    public void receive(String message) {

        // log.info("[kafka-received] ==> {}", message);

        // rocketMQTemplate.convertAndSend("prometheus-metric", message); // send 10w 消息，实际send 了 5w CPU 消耗一直保持在 60+

        // splitting and retrying (2147483647 attempts left). Error: MESSAGE_TOO_LARGE
        // 尝试设置 batch-size: 2000 错误；10000 错误；500000 错误
        // ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send("prometheus-metric-copy", message);

        // 测试消息消费 kafka
        if (start == 0) {
            start = System.currentTimeMillis();
        }
        if (counter.incrementAndGet() >= 100000) { // ~1s
            System.out.println(counter.get());
            System.out.println((System.currentTimeMillis() - start) / 1000);
            System.exit(1);
        }
    }
}

// 测试 rmq 消息消费
@Slf4j
@Component
// @RocketMQMessageListener(consumerGroup = "prometheus-consumer", topic = "prometheus-metric")
class RMQPrometheusListener implements RocketMQListener<String> {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static long start = 0;
    @Override
    public void onMessage(String message) {
        // log.info("[rocketmq-receive] ==> {}", message);
        if (start == 0) start = System.currentTimeMillis();
        if (counter.incrementAndGet() >= 100000) {
            System.out.println(counter.get());
            System.out.println((System.currentTimeMillis() - start) / 1000);
            System.exit(1);
        }
    }
}