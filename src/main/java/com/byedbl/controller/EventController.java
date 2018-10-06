package com.byedbl.controller;

import com.byedbl.event.TestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    @Autowired
    private ApplicationContext applicationContext;

    @ResponseBody
    @RequestMapping("event")
    public String publishEvent() {
        TestEvent event = new TestEvent("hi...","llo...");
        applicationContext.publishEvent(event);
        return "hello";
    }
}
