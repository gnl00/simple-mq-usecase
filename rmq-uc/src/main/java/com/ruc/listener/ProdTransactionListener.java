package com.ruc.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProdTransactionListener implements TransactionListener {

    private static final Map<String, Boolean> localDBMS = new ConcurrentHashMap<>();

    private final int waitTime;

    public ProdTransactionListener(int waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String transactionId = msg.getTransactionId();

        try {
            log.info("do something...");
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if ((Math.random() * 10) > 5) {
            log.info("[producer] local transaction COMMITTED");
            localDBMS.put(transactionId, true);
            return LocalTransactionState.COMMIT_MESSAGE;
        } else {
            log.info("[producer] local transaction ROLLBACK");
            localDBMS.put(transactionId, false);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    /**
     * <p>生产者收到消息回查后，需要检查对应消息的本地事务执行的最终结果
     * <p>生产者根据检查得到的本地事务的最终状态<strong>再次提交二次确认</strong>
     * <p>所以消费者需要注意处理消息重复消费的情况
     */
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
