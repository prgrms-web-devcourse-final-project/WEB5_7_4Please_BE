package com.deal4u.fourplease.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다."),
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 경매를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    INVALID_AUCTION_BIDDER(HttpStatus.BAD_REQUEST, "해당 사용자는 경매의 낙찰자가 아닙니다."),
    INVALID_ORDER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 타입입니다.");

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
