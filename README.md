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

## RMQ

### 前期准备

1、导入数据，使用天池[天猫推荐数据](https://tianchi.aliyun.com/dataset/140281)

之前以为 Jpa 的 saveAll 是批量新增方法，今天点进去源码一看：

```java
@Transactional
@Override
public <S extends T> List<S> saveAll(Iterable<S> entities) {
    Assert.notNull(entities, "Entities must not be null");
    List<S> result = new ArrayList<>();
    for (S entity : entities) {
        result.add(save(entity));
    }
    return result;
}
```

原来是循环调用 save 方法🤡怪不得每次插入大量数据的时候都这么慢，可恶！

优化方法可以使用下面几种：
1. 拼接批量插入 SQL
```sql
insert into table_name (column1, column2, column3, column4) 
values ("", "", "", ""), ("", "", "", ""), ("", "", "", "");
```

[参考](https://riun.xyz/work/3825161)

2. 使用 EntityManager#persist

需要标注 @Transactional，必须是 public 修饰的方法

```java
@PersistenceContext
private EntityManager entityManager;

@Transactional(rollbackOn = {Exception.class})
public int batchSave(List<Product> list) {
    int batchSize = 10000; // 设置组大小，分组插入
    boolean hasPersist = false;
    for (Product product : list) {
        entityManager.persist(product); // 减少一步查询操作
        int currCount = insertCount.incrementAndGet();
        hasPersist = true;

        if(currCount % batchSize == 0) {
            entityManager.flush();
            entityManager.clear();
            hasPersist = false;
        }
    }

    if(hasPersist) {
        entityManager.flush();
        entityManager.clear();
    }
    return 1;
}
```

[参考1](https://www.jianshu.com/p/a8ef0b04afa8/)

[参考2](https://www.jianshu.com/p/11153affb528)

SQL 拼接实现的时候比 EntityManager.persist 麻烦，但是实现的效果是最好的。数据量越大，SQL 拼接的优势越明显。

```java
// entityManager.persist // 25s for 10k data // 53634ms for 20k
// sqlConcat // 4s for 10k data // 6432ms for 20k 👍
```

### RMQ5

> 从 RocketMQ 5.x 版本出来到现在这么久了还没有支持 SpringBoot 3.x ...
> 使用过程中遇到的一个问题：使用 SpringBoot 3.x 导致 RocketMQ 消费者无法接收到消息，降级到 2.7.18 表现正常。

当前项目使用版本：
- spring-boot-starter-parent:2.7.18
- rocketmq-spring-boot-starter:2.3.0

### 分布式事务

- [ ] TODO

### RocketMQ 消费者方法注解

目前在使用 RocketMQ 的时候，需要在消费者类上标注 @RocketMQMessageListener 注解，一个消费者需要标注一次。**让我觉得有点不优雅**。

那么，能不能将类注解改成方法注解，让一个类中的多个方法能处理多个 topic 呢？

首先来看一下 @RocketMQMessageListener 的逻辑

- [ ] TODO


---

## 参考

* [Git 修改已提交内容的用户名和邮箱](https://segmentfault.com/a/1190000023612892)