package com.byedbl.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Bean2 {


    @Autowired
    private Bean1 bean1;

    public Bean2() {
        System.err.println("Bean2 的无参构造器");
    }

    public Bean1 getBean1() {
        return bean1;
    }

    public void setBean1(Bean1 bean1) {
        System.err.println();
        this.bean1 = bean1;
    }

    @PostConstruct
    public void studyPost() {
        System.out.println(Bean2.class.getName()+" PostConstruct...");
    }
}
