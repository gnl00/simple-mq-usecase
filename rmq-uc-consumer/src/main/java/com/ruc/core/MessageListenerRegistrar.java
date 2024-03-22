package com.ruc.core;

import com.ruc.anno.RMQListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class MessageListenerRegistrar {

    @Value("${rocketmq.name-server}")
    private String nameServer; // Obviously exist

    @Value("${rocketmq.endpoints}")
    private String endpoints; // Obviously exist

    @Autowired
    private ConfigurableEnvironment environment;

    public void registerContainer(Object targetObject, Method targetMethod, RMQListener ann) {
        String consumerGroup = environment.resolvePlaceholders(ann.consumerGroup());
        String topic = environment.resolvePlaceholders(ann.topic());
        String tag = environment.resolvePlaceholders(ann.tag());
        Class<?> messageType = ann.messageType();
        try {
            createConsumer(targetObject, targetMethod, consumerGroup, topic, tag, messageType);
        } catch (MQClientException e) {
            log.error("create mq client failed, error: {}", e.getMessage());
        }
    }

    private void createConsumer(Object targetObject, Method targetMethod, String consumerGroup, String topic, String tag, Class<?> messageType) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.subscribe(topic, tag);
        consumer.setNamesrvAddr(this.nameServer);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setMessageListener((MessageListenerConcurrently) (messageList, context) -> {
            for (MessageExt messageExt : messageList) {
                log.info("received message id: {}, body: {}, message type: {}", messageExt.getMsgId(), new String(messageExt.getBody(), StandardCharsets.UTF_8), messageType.getName());
                try {
                    targetMethod.invoke(targetObject, messageExt);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("emit mq listener failed, target object: {} method: {}, exception: {}", targetObject.toString(), targetMethod.getName(), e.getMessage());
                }
            }
            log.info("consume committed");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        log.info("## consumer for: {} created", targetMethod.getName());
        MessageListenerContainer.addConsumer(targetObject.getClass()+ "#" +targetMethod.getName(), consumer);
        log.info("## consumer for: {} registered", targetMethod.getName());
    }

}
