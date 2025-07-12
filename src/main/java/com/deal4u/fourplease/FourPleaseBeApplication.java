package com.deal4u.fourplease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableFeignClients(basePackages = "com.deal4u.fourplease.domain.payment")
@SpringBootApplication
public class FourPleaseBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FourPleaseBeApplication.class, args);
    }
}
