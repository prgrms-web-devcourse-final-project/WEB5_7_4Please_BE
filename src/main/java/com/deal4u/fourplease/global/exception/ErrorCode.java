package com.deal4u.fourplease.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다."),

    BID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 입찰 내역을 찾을 수 없습니다."),

    BID_FORBIDDEN_PRICE(HttpStatus.FORBIDDEN, "기존 입찰 금액보다 높은 금액을 입력해주세요."),

    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 경매를 찾을 수 없습니다."),

    AUCTION_NOT_OPEN(HttpStatus.FORBIDDEN, "해당 경매는 종료되었습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),

    WEBSOCKET_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WebSocket 메시지 전송 중 오류가 발생하였습니다."),

    WEBSOCKET_INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 요청 형식입니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

    EMPTY_LIST(HttpStatus.BAD_REQUEST, "빈 리스트 입니다."),

    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다."),
  
    BIDPERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "경매 기간을 찾을 수 없습니다.");


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
