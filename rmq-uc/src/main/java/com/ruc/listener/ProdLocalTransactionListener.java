package com.ruc.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ProdLocalTransactionListener implements TransactionListener {

    private static final Map<String, Boolean> localDBMS = new ConcurrentHashMap<>();

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String transactionId = msg.getTransactionId();

        if ((Math.random() * 10) > 5) {
            log.info("[producer] local transaction committed");
            localDBMS.put(transactionId, true);
            return LocalTransactionState.COMMIT_MESSAGE;
        } else {
            log.info("[producer] local transaction need to rollback");
            localDBMS.put(transactionId, false);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        log.info("[producer] re-checking local transaction status...");

        String transactionId = msg.getTransactionId();
        if (!localDBMS.containsKey(transactionId)) {
            return LocalTransactionState.UNKNOW;
        }

        if (localDBMS.get(transactionId)) {
            return LocalTransactionState.COMMIT_MESSAGE;
        } else {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }
}
