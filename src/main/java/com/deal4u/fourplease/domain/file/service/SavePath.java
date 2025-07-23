package com.deal4u.fourplease.domain.file.service;

public record SavePath(String filePath, String savedFileName) {

    private static final String SEPARATOR = "/";

    public String fullPath() {
        if (filePath.endsWith(SEPARATOR)) {
            return filePath + savedFileName;
        }
        return filePath + SEPARATOR + savedFileName;
    }
}
