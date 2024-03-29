package com.ruc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发送事务消息的时候需要配置生产者信息。
 * 因为如果 [service-b] 事务消费方本地事务执行失败，需要发送消息给 [service-a] 事务发起方，让进行事务回滚
 */

@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "${rmq-uc.consumer.group.tx}", topic = "${rmq-uc.topic.tx}")
public class TransactionMessageConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private static final Map<String, Boolean> CONSUMED_MSG_STORE = new ConcurrentHashMap<>();

    @Override
    public void onMessage(MessageExt msgExt) {
        String transactionId = msgExt.getTransactionId();
        if (msgExt.getReconsumeTimes() > 0 && CONSUMED_MSG_STORE.containsKey(transactionId) && CONSUMED_MSG_STORE.get(transactionId)) {
            log.info("transaction message with tx-id ==> {} already consumed", transactionId);
            return;
        }
        log.info("TransactionMessageConsumer received tx msg: {}", new String(msgExt.getBody(), StandardCharsets.UTF_8));
        log.info("transaction id ==> {}", transactionId);

        // do some transaction things
        // ...
        // 如果事务执行失败，本地事务回滚。此外还需要发送事务消息给 [事务发起方] 让它也进行回滚。
        // rocketMQTemplate.sendMessageInTransaction("RollbackTopic", null, null);

        CONSUMED_MSG_STORE.put(transactionId, true);
    }
}
