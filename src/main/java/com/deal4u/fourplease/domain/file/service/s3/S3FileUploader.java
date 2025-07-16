package com.deal4u.fourplease.domain.file.service.s3;

import com.deal4u.fourplease.global.exception.ErrorCode;
import java.io.InputStream;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
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

    public URL upload(InputStream data, String filename,
            S3MetaData metaData) {
        uploadFile(data, filename, metaData);
        return getPath(filename);
    }

    private void uploadFile(InputStream date, String filename, S3MetaData metaData) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .contentType(metaData.contentType())
                .contentLength(metaData.contentLength())
                .metadata(metaData.toMap())
                .build();
        RequestBody body = RequestBody.fromContentProvider(
                ContentStreamProvider.fromInputStream(date),
                metaData.contentType());
        try {
            s3Client.putObject(objectRequest, body);
        } catch (AwsServiceException | SdkClientException e) {
            throw ErrorCode.FILE_SAVE_FAILED.toException();
        }
    }

    private URL getPath(String filename) {
        GetUrlRequest request = GetUrlRequest.builder().bucket(bucketName).key(filename).build();
        return s3Client.utilities().getUrl(request);
    }
}
