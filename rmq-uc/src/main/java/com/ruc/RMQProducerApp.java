package com.ruc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RMQProducerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext ac = SpringApplication.run(RMQProducerApp.class, args);
    }
}
