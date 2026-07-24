package com.capstone.ai_insite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AiInsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiInsiteApplication.class, args);
    }

}
