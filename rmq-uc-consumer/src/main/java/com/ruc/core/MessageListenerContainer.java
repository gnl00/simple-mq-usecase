package com.ruc.core;

import org.apache.rocketmq.client.consumer.MQConsumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageListenerContainer {
    public static final Map<String, MQConsumer> CONSUMER_MAP = new ConcurrentHashMap<>();

    public static void addConsumer(String method, MQConsumer consumer) {
        CONSUMER_MAP.put(method, consumer);
    }

}
