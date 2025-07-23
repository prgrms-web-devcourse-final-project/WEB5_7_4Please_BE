package com.deal4u.fourplease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "com.deal4u.fourplease.domain.payment")
public class FourPleaseBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FourPleaseBeApplication.class, args);
    }
}
