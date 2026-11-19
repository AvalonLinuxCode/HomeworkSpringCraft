package com.eduplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.eduplatform.mapper")
public class EduPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduPlatformApplication.class, args);
    }
}
