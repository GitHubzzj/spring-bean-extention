package com.byedbl.lifecycle;

import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LifecycleConfig {

    @Bean("lifecycleProcessor")
    public Lifecycle lifeCycle() {
        return new MyLifecycle();
    }

}
