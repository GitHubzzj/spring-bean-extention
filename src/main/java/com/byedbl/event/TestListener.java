package com.byedbl.event;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

public class TestListener implements ApplicationListener<TestEvent> {
    @Async
    @Override
    public void onApplicationEvent(TestEvent event) {
        event.save();
    }
}
