package com.campusrunner;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.campusrunner.mapper")
@EnableScheduling
public class CampusRunnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusRunnerApplication.class, args);
    }
}
