package com.limuzi.limuziaicodeuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.limuzi.limuziaicodeuser.mapper")
@ComponentScan("com.limuzi")
//@EnableDubbo
public class LimuziAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(LimuziAiCodeUserApplication.class, args);
    }
}
