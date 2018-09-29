package com.byedbl.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfig {


    @Bean
    public TestListener testListener() {
        return new TestListener();
    }

}
