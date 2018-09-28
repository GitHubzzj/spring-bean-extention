package com.byedbl.beans;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class Person implements BeanNameAware, BeanFactoryAware,
        ApplicationContextAware, InitializingBean, DisposableBean {

    private String name;

    public Person() {
        System.err.println("Person类构造方法");
    }


    public String getName() {
        System.err.println("Person.getName 方法被调用");
        return name;
    }

    public void setName(String name) {
        this.name = name;
        System.err.println("Person.setName方法被调用");
    }

    @PostConstruct
    public void myInit() {
        System.err.println("Person.myInit被调用 PostConstruct");
    }

    //自定义的销毁方法
    @PreDestroy
    public void myDestroy() {
        System.err.println("Person.myDestroy被调用 PreDestroy");
    }

    @Override
    public void destroy() throws Exception {
        System.err.println("DisposableBean.Person.destory被调用");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.err.println("InitializingBean.Person.afterPropertiesSet被调用");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        System.err.println("ApplicationContextAware.Person.setApplicationContext被调用");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.err.println("BeanFactoryAware.setBeanFactory被调用 "+beanFactory);
    }

    @Override
    public void setBeanName(String beanName) {
        System.err.println("BeanNameAware.Person.setBeanName被调用,beanName:" + beanName);
    }

    @Override
    public String toString() {
        return "Person.name is :" + name;
    }

}

