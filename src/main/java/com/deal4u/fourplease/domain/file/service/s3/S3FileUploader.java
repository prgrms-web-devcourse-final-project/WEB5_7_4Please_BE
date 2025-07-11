package com.deal4u.fourplease.domain.file.service.s3;

import java.io.InputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@Slf4j
public class S3FileUploader {

    private final S3Client s3Client;

    private final String bucketName;

    public String upload(InputStream date, String filename,
            Map<String, String> metaData) {
        uploadFile(date, filename, metaData);
        return getPath(filename);
    }

    private void uploadFile(InputStream date, String filename, Map<String, String> metaData) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .metadata(metaData)
                .build();
        RequestBody body = RequestBody.fromContentProvider(
                ContentStreamProvider.fromInputStream(date),
                metaData.get("Content-Type"));
        s3Client.putObject(objectRequest, body);
    }

    private String getPath(String filename) {
        GetUrlRequest request = GetUrlRequest.builder().bucket(bucketName).key(filename).build();
        return s3Client.utilities().getUrl(request).toString();
    }
}
