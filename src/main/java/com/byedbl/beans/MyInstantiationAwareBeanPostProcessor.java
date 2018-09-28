package com.byedbl.beans;


import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;

@Component
public class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        System.err.println("MyInstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation "+beanName);
        return null;
    }
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        System.err.println("MyInstantiationAwareBeanPostProcessor.postProcessAfterInstantiation "+beanName);
        return true;
    }

    @Nullable
    @Override
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

        System.err.println("MyInstantiationAwareBeanPostProcessor.postProcessPropertyValues "+beanName);
        return pvs;
    }
}