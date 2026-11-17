package com.greenops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GreenOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreenOpsApplication.class, args);
    }
}
