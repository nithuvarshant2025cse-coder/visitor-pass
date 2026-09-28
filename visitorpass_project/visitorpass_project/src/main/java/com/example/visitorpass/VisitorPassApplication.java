package com.example.visitorpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisitorPassApplication {
    public static void main(String[] args) {
        SpringApplication.run(VisitorPassApplication.class, args);
    }
}
