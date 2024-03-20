package com.ruc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RUCConsumerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext ac = SpringApplication.run(RUCConsumerApp.class, args);
    }
}
