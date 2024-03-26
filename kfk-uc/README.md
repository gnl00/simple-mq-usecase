# kfk-uc

原先这个模块的预想是通过 Prometheus 的 HTTP API 从 Prometheus 拉取监控信息然后 push 到 Kafka 上，用来测试 Kafka 的吞吐量。

后来发现了 prometheus_kafka_adapter，自带从 Prometheus 推送到 Kafka 上的能力，这个模块就不需要添加这个功能了。

后来改成借助 micrometer-registry-prometheus 将当前模块的监控信息注册到 Prometheus 上。

最后...拿来测试 Kafka 的消息消费能力，测试结果：5s 可以消费 10w+ 消息（当然这和运行的机器有很大关系）和 RocketMQ 比起来呢？