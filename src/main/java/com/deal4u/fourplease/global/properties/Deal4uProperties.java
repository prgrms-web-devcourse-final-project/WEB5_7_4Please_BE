package com.deal4u.fourplease.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "deal4u")
public class Deal4uProperties {

    private final Deal4uProperties.S3 s3 = new S3();

    @Getter
    @Setter
    public static class S3 {

        private String accessKey;
        private String secretKey;
        private String region;
        private String bucketName;
    }
}
