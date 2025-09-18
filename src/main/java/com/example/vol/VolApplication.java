package com.example.vol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class VolApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolApplication.class, args);
    }

}
