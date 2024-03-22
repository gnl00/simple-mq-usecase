package com.ruc.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Essentially is a consumer
 */
@Slf4j
@Component
public class MessageListenerContainer implements SmartLifecycle {

    /**
     * You can put configurations of mq consumer here as a holder as well
     */

    public static Map<String, DefaultMQPushConsumer> CONSUMER_MAP = new ConcurrentHashMap<>();

    private static boolean IS_RUNNING = false;

    public static void addConsumer(String method, DefaultMQPushConsumer consumer) {
        CONSUMER_MAP.put(method, consumer);
    }

    @Override
    public void start() {
        log.info("## SmartLifecycle#start");
        if (!CollectionUtils.isEmpty(CONSUMER_MAP)) {
            CONSUMER_MAP.forEach((method, consumer) -> {
                try {
                    consumer.start();
                    log.info("## consumer for: {} started", method);
                } catch (MQClientException e) {
                    log.error("[ERROR] consumer for: {} start failed, caught exception: {}", method, e.getMessage());
                }
            });
            IS_RUNNING = true;
        }
    }

    @Override
    public void stop() {
        if (!CollectionUtils.isEmpty(CONSUMER_MAP)) {
            CONSUMER_MAP.forEach((method, consumer) -> {
                consumer.shutdown();
            });
        }
        IS_RUNNING = false;
    }

    @Override
    public boolean isRunning() {
        return IS_RUNNING;
    }
}
