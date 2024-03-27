# kfk-uc

原先这个模块的预想是通过 Prometheus 的 HTTP API 从 Prometheus 拉取监控信息然后 push 到 Kafka 上，用来测试 Kafka 的吞吐量。

后来发现了 prometheus_kafka_adapter，自带从 Prometheus 推送到 Kafka 上的能力，这个模块就不需要添加这个功能了。

后来改成借助 micrometer-registry-prometheus 将当前模块的监控信息注册到 Prometheus 上。

最后...拿来测试 Kafka 的吞吐能力。

环境：
* 处理器：2 GHz 四核 Intel Core i5 八代
* 内存：16 GB 3733 MHz LPDDR4X

> MQ 参数均为默认，结果当作一个参考即可，别太较真～

1、消息生产

* Kafka 吞 10w 消息 cpu~5%/mem~12%/time~1s
* RocketMQ 吞 10w 消息 cpu~65+%/mem~25%/time~108s

2、消息消费

* 10w 消息消费所花费的时间都差不多（这里测试的消息都是比较简单的消息）
