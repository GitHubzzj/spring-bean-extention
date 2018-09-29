package com.byedbl.lifecycle;


import org.springframework.context.LifecycleProcessor;


public class MyLifecycle implements LifecycleProcessor {
    @Override
    public void start() {
        System.out.println("MyLifecycle start...");
    }

    @Override
    public void stop() {
        System.out.println("MyLifecycle stop...");
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void onRefresh() {
        System.out.println("MyLifecycle onRefresh...");
    }

    @Override
    public void onClose() {
        System.out.println("MyLifecycle onClose...");
    }
}
