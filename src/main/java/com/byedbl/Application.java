package com.byedbl;

import com.byedbl.event.TestEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@ComponentScan("com.byedbl")
public class Application {


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.out.println("=========================event start===============================");
        TestEvent event = new TestEvent("hello", "msg");

        context.publishEvent(event);
        System.out.println("=========================event   end===============================");

        // 测试 MyLifecycle , PreDestroy, destory 可打开
//        context.close();
    }
}
