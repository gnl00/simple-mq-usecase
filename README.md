# simple-mq-usecase

**前言**

> 由于在 MQ 的使用上经验比较少，
> 遇到的问题也仅有类似：RocketMQ 同一个消费者组设置两个不同的 topic 导致无法正确消费。
> 这样子比较简单的问题。因此本项目主要目的就是收集一些 MQ 的最佳实践以及不同 MQ 的对比。

## RocketMQ & Kafka

RocketMQ 与 Kafka 到底有什么区别？

### 架构上

首先在架构上两者是很相似的：
* 轻量级注册中心，RMQ：NameServer；Kafka：Zookeeper or KRaft
* 消息存储中心 Broker
* 生产者、生产者组
* 消费者、消费者组
* 消息 Topic
* 消息队列（分区），分别是 RMQ --> MessageQueue 和 Kafka --> Partition
* 消息消费：都是通过消费点位 Offset 从消息队列（Kafka 从分区 Segment）中进行消费
* 高效读写支持：都使用了 PageCache 技术；都使用了零拷贝技术（mmap 和 sendfile）。
（按理来说 mmap 和 sendfile 都是零拷贝的实现方式，不知道为什么网上那么多文章把零拷贝和 mmap 分开来说...？）
* ...

### Kafka 特性

一些 Kafka 的特性：
* 生产者 ACK
* ExactlyOnce 语义 = At least one + 消息发送幂等性
* 消息存储：将Partition 分段，分成多个 Segment。采用分片+索引存储消息文件，
类似
```text
00000000000000000000.index
00000000000000000000.log
00000000000000170410.index
00000000000000170410.log
00000000000000239430.index
00000000000000239430.log
```
* 主副本通过**高低水位**进行消息同步消费
* 消息顺序读写

### RocketMQ 特性

一些 RocketMQ 的特性：
* 分布式事务消息（并不是说 Kafka 不支持事务消息，只是在这方面的实现确实不错）
* DLedger 基于 Raft 协议的分布式日志存储组件

### 应用场景
* 大数据场景下使用 kafka。老生常谈，因为 Kafka 消息吞吐量比 RocketMQ 大
* 日志收集场景下使用 Kafka。
* 大多数业务场景以及分布式事务使用 RocketMQ。因为实现较多，以及...大厂背书 :)

## 常见问题

1、如何加快生产者消息发送效率？提高消息吞吐量？
* 消息批量发送
* 消息压缩发送，RMQ 和 Kafka 都支持（消息体压缩也会带来一定的 CPU 计算压力与消息延迟）

2、如何保证消息一定发送成功？也可以这么问：如何防止消息丢失？

...

3、如何防止消息重复消费？

...

## Use Case

### RMQ 事务消息

1、导入数据，使用天池[天猫推荐数据](https://tianchi.aliyun.com/dataset/140281)

TODO 大文件读取并入库（1.7G）

## 参考

...