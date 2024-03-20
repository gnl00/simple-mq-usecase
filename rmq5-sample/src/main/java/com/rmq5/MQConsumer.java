package com.rmq5;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;

import java.util.Collections;

public class MQConsumer {
    public static void main(String[] args) {
        String endpoint = "localhost:8081";
        // 需要先创建 topic
        // 否则报错 No topic route info in name server for the topic: TestTopic
        String topic = "TestTopic";
        String consumerGroup = "myConsumerGroup";

        ClientConfiguration clientConfiguration = ClientConfiguration
                .newBuilder()
                .setEndpoints(endpoint)
                .build();


        String tag = "*";
        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);

        ClientServiceProvider provider = ClientServiceProvider.loadService();
        try {
            PushConsumer pushConsumer = provider.newPushConsumerBuilder()
                    .setClientConfiguration(clientConfiguration)
                    .setConsumerGroup(consumerGroup)
                    .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
                    .setMessageListener(messageView -> {
                        System.out.println("consumer received message: " + messageView.getMessageId());
                        return ConsumeResult.SUCCESS;
                    })
                    .build();
            System.out.println("\n");
        } catch (ClientException e) {
            System.out.println("consume message failed: " + e.getMessage());
        }

    }
}
