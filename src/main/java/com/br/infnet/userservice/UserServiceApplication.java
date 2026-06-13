package com.br.infnet.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class UserServiceApplication {
    static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
