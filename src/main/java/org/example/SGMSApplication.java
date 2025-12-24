package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.mapper")  // 扫描所有Mapper接口
public class SGMSApplication {
    public static void main(String[] args) {
        SpringApplication.run(SGMSApplication.class, args);
    }
}