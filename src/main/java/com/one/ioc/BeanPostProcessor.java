package com.one.ioc;

import org.springframework.beans.BeansException;

/**
 * @author One
 * @description
 * @date 2018/11/01
 */
public class BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(bean + "前置处理" + beanName);
        return null;
    }

    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(bean + "后置处理" + beanName);
        return null;
    }
}
