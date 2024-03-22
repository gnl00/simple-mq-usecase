package com.ruc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发送事务消息的时候需要配置生产者信息。
 * 因为如果 [service-b] 事务消费方本地事务执行失败，需要发送消息给 [service-a] 事务发起方，让进行事务回滚
 */

@Slf4j
@Component
@RocketMQTransactionListener
public class TransactionConsumer implements RocketMQLocalTransactionListener {

    // Presume this is db
    private static final Map<String, Boolean> localDBMS = new ConcurrentHashMap<>();

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // parsing json string to java object
        log.info("executing [consumer] local transaction...");
        if ((Math.random() * 10) > 5) {
            log.info("[consumer] local transaction committed");
            String rmqTxGid = (String) msg.getHeaders().get("RMQ_TX_GID");
            localDBMS.put(rmqTxGid, true);
            return RocketMQLocalTransactionState.COMMIT;
        }
        log.info("[consumer] local transaction executed failed");
        return RocketMQLocalTransactionState.ROLLBACK;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        log.info("[consumer] re-check local transaction status");
        String rmqTxGid = (String) msg.getHeaders().get("RMQ_TX_GID");
        if (localDBMS.containsKey(rmqTxGid)) return RocketMQLocalTransactionState.UNKNOWN;

        if (localDBMS.get(rmqTxGid)) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
