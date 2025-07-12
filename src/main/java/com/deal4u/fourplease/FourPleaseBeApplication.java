package com.deal4u.fourplease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class FourPleaseBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FourPleaseBeApplication.class, args);
    }

}
