package com.ruc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RMQMain {
    public static void main(String[] args) {
        ConfigurableApplicationContext ac = SpringApplication.run(RMQMain.class, args);
        for (String beanDefinitionName : ac.getBeanDefinitionNames()) {
            System.out.println(beanDefinitionName);
        }
    }
}
