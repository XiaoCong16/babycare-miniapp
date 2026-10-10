package com.example.babycare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BabycareApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabycareApplication.class, args);
    }
}
