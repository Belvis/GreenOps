package com.greenops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GreenOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenOpsApplication.class, args); // <-- C'est .class ici, pas .java !
    }
}