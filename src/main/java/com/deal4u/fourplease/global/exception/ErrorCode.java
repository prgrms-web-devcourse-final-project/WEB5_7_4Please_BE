package com.deal4u.fourplease.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    public GlobalException toException() {
        return new GlobalException(status, message);
    }

    public GlobalException toException(String message) {
        return new GlobalException(status, message);
    }

    public GlobalException toException(String message, Object... args) {
        return new GlobalException(status, message, args);
    }

    public GlobalException toException(Throwable cause) {
        return new GlobalException(cause, status, message);
    }

    public GlobalException toException(Throwable cause, String message) {
        return new GlobalException(cause, status, message);
    }

    public GlobalException toException(Throwable cause, String message, Object... args) {
        return new GlobalException(cause, status, message, args);
    }
}
