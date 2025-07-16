package com.deal4u.fourplease.domain.file.config;

import com.deal4u.fourplease.domain.file.service.FileSaver;
import com.deal4u.fourplease.domain.file.service.s3.FileValidator;
import com.deal4u.fourplease.domain.file.service.s3.S3FileSaver;
import com.deal4u.fourplease.domain.file.service.s3.S3FileUploader;
import com.deal4u.fourplease.global.properties.Deal4uProperties;
import com.deal4u.fourplease.global.properties.Deal4uProperties.S3;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(prefix = "deal4u.s3", name = {"access_key", "secret_key", "region"})
@RequiredArgsConstructor
public class S3Config {

    private final Deal4uProperties deal4uProperties;

    @Bean
    public FileSaver fileSaver() {
        return new S3FileSaver(fileValidator(), s3FileUploader(s3Client()));
    }

    @Bean
    public S3FileUploader s3FileUploader(S3Client s3Client) {
        S3 s3 = deal4uProperties.getS3();
        return new S3FileUploader(s3Client, s3.getBucketName());
    }

    @Bean
    public S3Client s3Client() {
        Deal4uProperties.S3 s3 = deal4uProperties.getS3();

        return S3Client.builder()
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        s3.getAccessKey(),
                                        s3.getSecretKey()
                                )
                        )
                )
                .build();
    }

    @Bean
    public FileValidator fileValidator() {
        return new FileValidator();
    }
}
