package com.example.greenscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GreenSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenSchedulerApplication.class, args);
    }

}
