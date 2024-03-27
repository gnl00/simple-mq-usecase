package com.kuc.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        // 添加自定义监控指标
        // 执行请求 http://localhost:9091/actuator/prometheus 即可看到
        // 添加到 MeterRegistry 的 tag 可以看成公共 tag，大多数监控都会包含这部分公共的 tag
        meterRegistry.config()
                .commonTags("foo", "bar")
                .commonTags("region", "hangzhou"); // key -> value
    }

    /**
     * @Timed 记录了发生的事件的数量和这些事件的总耗时
     * @Counted 记录方法执行总量或者计数值，适用于一些单向增长类型的统计，例如下单、支付次数、接口请求总量记录等
     * 先执行请求 http://localhost:8089/metrics/v1 执行到 MetricsController#index 方法
     * 标注在方法上的 @Timed 中的属性才会被记录
     */
    @Timed(value = "v1", description = "visit http://localhost:9091/actuator/metrics/v1", histogram = true)
    // @Counted
    @GetMapping("/index")
    public void index() {
        log.info("request for MetricsController#index");
        // Gauge 表示一个可以任意上下浮动的单数值度量 Meter，Gauge 通常用于变动的测量值，如当前的内存使用情况或运行状态中的线程数
        // Metrics.gauge();
    }

    @Timed(value = "v2", description = "visit http://localhost:9091/actuator/metrics/v2", histogram = true)
    @GetMapping("/about")
    public void about() {
        log.info("request for MetricsController#about");
    }
}
