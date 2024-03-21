package com.ruc.core;

import com.ruc.anno.RMQListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class MessageListenerPostProcessor implements ApplicationContextAware, BeanPostProcessor, InitializingBean {

    private ApplicationContext applicationContext;

    private MessageListenerRegistrar messageListenerRegistrar;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        this.messageListenerRegistrar = this.applicationContext.getBean(MessageListenerRegistrar.class);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        for (Method targetMethod : declaredMethods) {
            RMQListener ann = targetMethod.getAnnotation(RMQListener.class);
            if (null != ann) {
                log.debug("method: {} belongs to: {}, handle: {}:{}", targetMethod.getName(), ann.consumerGroup(), ann.topic(), ann.tag());
                targetMethod.setAccessible(true);
                messageListenerRegistrar.registerContainer(bean, targetMethod, ann);
            }
        }
        return bean;
    }
}
