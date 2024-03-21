package com.ruc.core;

import org.apache.rocketmq.client.consumer.MQConsumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Essentially is a consumer
 */
public class MessageListenerContainer {

    /**
     * You can put configurations of mq consumer here as a holder as well
     */

    public static final Map<String, MQConsumer> CONSUMER_MAP = new ConcurrentHashMap<>();

    public static void addConsumer(String method, MQConsumer consumer) {
        CONSUMER_MAP.put(method, consumer);
    }

}
