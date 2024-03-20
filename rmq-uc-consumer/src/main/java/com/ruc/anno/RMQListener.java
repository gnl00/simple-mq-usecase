package com.ruc.anno;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RMQListener {
    String nameSrv() default "localhost:9876";
    String consumerGroup();
    String topic();

    String tag() default "*";

    Class<?> messageType();
}
