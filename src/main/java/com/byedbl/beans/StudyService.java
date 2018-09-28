package com.byedbl.beans;


import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StudyService {

    @PostConstruct
    public void studyPost() {
        System.out.println(StudyService.class.getName()+" PostConstruct...");
    }
}
