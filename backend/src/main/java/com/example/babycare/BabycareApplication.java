package com.example.babycare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.babycare.infrastructure.persistence.mapper")
public class BabycareApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabycareApplication.class, args);
    }
}
