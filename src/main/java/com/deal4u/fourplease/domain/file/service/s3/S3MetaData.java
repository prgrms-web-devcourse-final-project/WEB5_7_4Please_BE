package com.deal4u.fourplease.domain.file.service.s3;

import java.util.Map;

public record S3MetaData(String contentType, String contentLength) {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";

    public Map<String, String> toMap() {
        return Map.of(CONTENT_TYPE, contentType, CONTENT_LENGTH, contentLength);
    }
}
