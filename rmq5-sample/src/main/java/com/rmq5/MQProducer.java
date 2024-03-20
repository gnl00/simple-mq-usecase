package com.rmq5;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MQProducer {
    public static void main(String[] args) throws ClientException {
        String endpoint = "localhost:8081";
        // 需要先创建 topic
        // 否则报错 No topic route info in name server for the topic: TestTopic
        String topic = "TestTopic";

        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
        ClientConfiguration configuration = builder.build();

        ClientServiceProvider provider = ClientServiceProvider.loadService();
        Producer producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();

        Message message = provider.newMessageBuilder()
                .setBody("hello~".getBytes(StandardCharsets.UTF_8))
                .setKeys("message-key")
                .setTopic(topic)
                .setTag("message-tag")
                .build();

        try {
            SendReceipt sendReceipt = producer.send(message);
            System.out.println("send message successfully, messageId: " + sendReceipt.getMessageId());

        } catch (ClientException e) {
            System.out.println("send message failed");
        } finally {
            try {
                if (Objects.nonNull(producer)) producer.close();
            } catch (IOException e) {
                System.out.println("producer close exception");
            }
        }
    }
}
