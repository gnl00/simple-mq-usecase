package com.kuc;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootApplication
public class KafkaLoggingApp {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static long start = 0;
    public static void main(String[] args) {
        SpringApplication.run(KafkaLoggingApp.class, args);
    }

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Uncomment the line below to consume message
    @KafkaListener(id = "${spring.kafka.consumer.client-id}", groupId = "${spring.kafka.consumer.group-id}", topics = {"prometheus-metric"})
    public void receive(String message) {
        if (start == 0) {
            start = System.currentTimeMillis();
        }

        // log.info("[received] ==> {}", message);


        // rocketMQTemplate.convertAndSend("prometheus-metric", message); // send 10w 消息，实际send 了 5w CPU 消耗一直保持在 60+

        // splitting and retrying (2147483647 attempts left). Error: MESSAGE_TOO_LARGE
        // 尝试设置 batch-size: 2000 错误；10000 错误；500000 错误
        ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send("prometheus-metric-copy", message);

        if (counter.incrementAndGet() >= 100000) {
            System.exit(1);
        }
//        long end = System.currentTimeMillis();
//        if ((end - start) / 1000 >= 10) { // 5s 能消费 10w+
//            System.out.println(start);
//            System.out.println(end);
//            System.out.println(counter.get());
//            System.exit(1);
//        }
    }

}
