package com.matching.team_builder;  // 기존

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.matching.team_builder", "com.capstone.backend"})  // 추가
public class TestController {
    public static void main(String[] args) {
        SpringApplication.run(TeamBuilderApplication.class, args);
    }
}
