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

## 前期准备

### 数据导入

导入数据，使用天池[天猫推荐数据](https://tianchi.aliyun.com/dataset/140281)

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

## RocketMQ

> rocketmq 本地部署推荐 [xuchengen/rocketmq](https://hub.docker.com/r/xuchengen/rocketmq) 纵享丝滑

```shell
docker run -itd \
 --name=rocketmq02 \
 --hostname rocketmq02 \
 --restart=always \
 -p 8081:8080 \
 -p 9876:9876 \
 -p 10909:10909 \
 -p 10911:10911 \
 -p 10912:10912 \
 -v rocketmq_data:/home/app/data \
 -v /etc/localtime:/etc/localtime \
 -v /var/run/docker.sock:/var/run/docker.sock \
 xuchengen/rocketmq:latest
```

> 从 RocketMQ 5.x 版本出来到现在这么久了还没有支持 SpringBoot 3.x ...
> 使用过程中遇到的一个问题：使用 SpringBoot 3.x 导致 RocketMQ 消费者无法接收到消息，降级到 2.7.18 表现正常。

当前项目使用版本：
- spring-boot-starter-parent:2.7.18
- rocketmq-spring-boot-starter:2.3.0

RMQ 官方不推荐自动创建 topic，不好管理。推荐手动创建 Topic
```shell
sh bin/mqadmin updatetopic -n localhost:9876 -t TestTopic -c DefaultCluster
```

### RocketMQ 消费者方法注解

目前在使用 RocketMQ 的时候，需要在消费者类上标注 @RocketMQMessageListener 注解，一个消费者需要标注一次。

那么，能不能将类注解改成方法注解，让一个类中的多个方法能处理多个 topic 呢？

来看一下 @RocketMQMessageListener 的逻辑。

让 @RocketMQMessageListener 能够实现对应的功能实际上是下面三个类共同实现的：
* RocketMQMessageListenerBeanPostProcessor
* RocketMQMessageListenerContainerRegistrar
* DefaultRocketMQListenerContainer

首先通过 RocketMQMessageListenerBeanPostProcessor 捕捉到标注有 @RocketMQMessageListener 的类，根据注解信息使用 RocketMQMessageListenerContainerRegistrar 注册 DefaultRocketMQListenerContainer。

DefaultRocketMQListenerContainer 就可以粗略的看成是一个消费者，因为消费者的初始化是在 DefaultRocketMQListenerContainer 中进行的，并且 DefaultRocketMQListenerContainer 还持有了消费者对象本身以及该消费者的配置。

...

按照 @RocketMQMessageListener 的思路，我们也可以自定义实现一个 @RMQListener，使其可以标注在方法上，然后遍历方法创建消费者即可。

> 已实现，使用 @RMQListener 注解标注在方法上即可。

...

> Rocketmq 中的 @RocketMQMessageListener 只能标注在类上。能标注在方法上的话就可以在一个类中处理多个 Topic 或者多个 Tag，为什么不设计成可以标注在方法上？这是基于什么原因考虑的？
> 
> 当将 @RocketMQMessageListener 注解标注在类上时，可以将同一个 Topic 或者多个 Tag 的消息交给同一个类来处理。
> 这样做的好处是可以将相关的消息处理逻辑封装在一个类中，提高代码的可读性和可维护性。同时，在类级别上标注 @RocketMQMessageListener 
> 还可以方便地管理消息消费者的生命周期，并且可以避免在每个方法上都声明一遍 @RocketMQMessageListener。
>
> 如果将 @RocketMQMessageListener 注解标注在方法上，就会导致一个类中可能存在多个方法来处理同一个 Topic 或者多个 Tag 的消息。
> 这样会增加代码的复杂性，不利于代码的维护和理解。另外，如果每个方法都需要声明 @RocketMQMessageListener，可能会导致代码冗余和混乱。
> 因此，将 @RocketMQMessageListener 注解设计成只能标注在类上，是为了提高代码的可读性、可维护性和一致性。
> from gpt

> 之所以有这个想法是因为之前做的某个项目中看到了这样的用法，所以研究了一下...

### 同一个消费者组 Topic 订阅

<p style="color: red">同一个消费者组不能订阅不同的 Topic 👹</p>

> <p style="color: coral">是的，这是第二次踩这个坑了。</p>

### 分布式事务

> 这篇文章写得还不错：[分布式事务](https://github.com/allentofight/easy-cs/blob/main/%E5%88%86%E5%B8%83%E5%BC%8F/%E5%88%86%E5%B8%83%E5%BC%8F%E4%BA%8B%E5%8A%A1%EF%BC%8C%E7%9C%8B%E8%BF%99%E7%AF%87%E7%9C%9F%E7%9A%84%E5%A4%9F%E4%BA%86!.md)

<p style="color: red">发送事务消息的时候需要在消费者服务同时配置生产者信息。</p>
因为如果 [service-b] 事务消费方本地事务执行失败，需要发送消息给 [service-a] 事务发起方，让其进行事务回滚。

配置

```yaml
rocketmq:
  name-server: localhost:9876
  endpoints: localhost:8081
  consumer:
    group: rmq-uc-consumer
    pull-batch-size: 30
  producer:
    group: rmq-uc-prod
    send-message-timeout: 300000

rmq-uc:
  topic:
    test: TestTopic
    dev: DevTopic
    tx: TxTopic
  producer:
    group:
      tx: ${rocketmq.producer.group}-tx
      dev: ${rocketmq.producer.group}-dev
      test: ${rocketmq.producer.group}-test
  consumer:
    group:
      tx: ${rocketmq.consumer.group}-tx
      dev: ${rocketmq.consumer.group}-dev
      test: ${rocketmq.consumer.group}-test
```

```java
// 生产者具体实现
// [rmq-uc]#com.ruc.service.ProducerService
// [rmq-uc]#com.ruc.listener.ProdTransactionListener

// 消费者具体实现
// [rmq-uc-consumer]#com.ruc.consumer.TransactionMessageConsumer
```

---

**使用心得**

生产者收到消息回查后，需要检查对应消息的本地事务执行的最终结果。

生产者根据检查得到的本地事务的最终状态**再次提交二次确认**。所以<span style="color: red">消费者需要注意处理消息重复消费的情况</span>。

---

## Docker & OrbStack 网络映射

> 这一节是在使用 Kafka 的时候遇到的坑，思考了一下应该把它放在 Kafka 前面比较好，避免后面踩坑。

当前项目是在 macOS 下进行开发的，使用 homebrew 来安装 docker，使用 OrbStack 来作为 docker 管理工具。在开发过程中踩了一些 docker 网络映射的坑。

首先将 node_exporter 安装在宿主机之后，通过 docker 部署 Prometheus，发现 Prometheus 配置

```yaml
scrape_configs:
  - job_name: "env-monitor"
    static_configs:
      - targets: ["localhost:9100"]
```

<p style="color: red">无法访问到宿主机网络。</p>

需要修改成 `dokcer.for.mac.localhost:9100` 才可以正常访问。

除了使用 `dokcer.for.mac.localhost` 还有一个解决办法就是使用 `--net=host` 参数。
将 Docker 容器网络与宿主机的网络从默认的 `--net=bridge` 桥接模式修改成 `--net=host` 主机模式，这样一来就可以通过 `localhost` 直接访问到宿主机网络。

```shell
# 指定了 --net=host 的情况下 -p 8080:8080 是不生效的
# 什么意思呢？在指定了 --net=host 的情况下 -p 9090:8080 的话无法通过 localhost:9090 访问，端口映射会失效，只能通过 localhost:8080 访问
# 我在这里只是做一个标记
docker run -it -p 8080:8080 --net=host ...
```

...

因为在这一小节中使用到的服务：
* kafka
* kafka-ui
* prometheus
* prometheus_kafka_adapter

都是使用 Docker 来进行部署的，所以在部署的时候最好加上 `--net=host` 参数防止网络连接出现异常。

---

## Kafka

Kafka 最常见的一个使用场景可能就是日志收集了，本次使用 [node_exporter](https://github.com/prometheus/node_exporter) 收集当前机器上的运行日志。

> macOS 使用 OrbStack 作为 Docker 客户端的时候需要使用 `docker.for.mac.localhost` 来进行对宿主机的访问。

### Prometheus

1、配置修改

修改 `prometheus.yml` 配置
```yaml
# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: "prometheus"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["docker.for.mac.localhost:9090"]
  - job_name: "env-monitor"
    static_configs:
      - targets: ["docker.for.mac.localhost:9100"]

```

docker 部署

```shell
docker run \
    --net=host \
    -p 9090:9090 \
    -v ./prometheus.yml:/etc/prometheus/prometheus.yml -d --name=prmth \
    prom/prometheus
```

2、将自定义应用暴露给 prometheus 监控，[参考](https://help.aliyun.com/zh/prometheus/use-cases/connect-spring-boot-applications-to-managed-service-for-prometheus)

### 日志收集

1、[docker 启动 kafka](https://kafka.apache.org/documentation/#docker)

```shell
docker pull apache/kafka:latest
docker run --name=kafka -d --net=host -p 9092:9092 apache/kafka:latest
```

2、创建 topic

```shell
/opt/kafka/bin$ sh kafka-topics.sh --list --bootstrap-server=localhost:9092
/opt/kafka/bin$ sh kafka-topics.sh --create --topic=prometheus-metric --bootstrap-server=localhost:9092
```

3、将 Prometheus 收集到的数据写到 Kafka

部署 prometheus_kafka_adapter

```shell
docker run -d --name prometheus-kafka-adapter-01 --restart=always -m 2g \
--net=host \
-e KAFKA_BROKER_LIST=localhost:9092 \
-e KAFKA_TOPIC=prometheus-metric \
-e PORT=10401 \
-e SERIALIZATION_FORMAT=json \
-e GIN_MODE=release \
-e LOG_LEVEL=debug \
-p 10401:10401 \
telefonica/prometheus-kafka-adapter:1.9.0
```

数据流向如下

```shell
prometheus_xxx_exporter ==> prometheus ==> prometheus_kafka_adapter ==> kafka 存放一段时间 ==> DB
```

部署情况如下：

* kafka docker 部署
* node_exporter 宿主机部署
* [kafka_exporter](https://github.com/danielqsj/kafka_exporter) docker 部署
* prometheus docker 部署
* prometheus_kafka_adapter docker 部署

### Kafka WebUI

* [kafka-ui](https://github.com/provectus/kafka-ui)
* [kafdrop](https://github.com/obsidiandynamics/kafdrop)

使用 kafka-ui：
```shell
# 指定了 --net=host 的情况下 -p 8080:8080 是不生效的
# 什么意思呢？在指定了 --net=host 的情况下 -p 9090:8080 的话无法通过 localhost:9090 访问，端口映射会失效，只能通过 localhost:8080 访问
# 我在这里只是做一个标记
docker run -it -p 8080:8080 --net=host -e DYNAMIC_CONFIG_ENABLED=true provectuslabs/kafka-ui
```

### Kafka Error: MESSAGE_TOO_LARGE

* [kafka调整消息大小](https://zhuanlan.zhihu.com/p/433515452)

---

## 参考

* [Git 修改已提交内容的用户名和邮箱](https://segmentfault.com/a/1190000023612892)
* [Docker 网络参数](https://yeasy.gitbook.io/docker_practice/underly/network)