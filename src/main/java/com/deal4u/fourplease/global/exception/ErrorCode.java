package com.deal4u.fourplease.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다."),

    //Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_AUTH_HEADER(HttpStatus.BAD_REQUEST, "Authorization 헤더 형식이 잘못되었습니다."),
    TOKEN_ALREADY_BLACKLISTED(HttpStatus.FORBIDDEN, "이미 블랙리스트 처리된 토큰입니다."),

    //Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일의 회원이 존재하지 않습니다."),

    //Member - NickName
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 공백일 수 없습니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.UNPROCESSABLE_ENTITY, "이미 존재하는 닉네임입니다."),
    NICKNAME_LENGTH_INVALID(HttpStatus.BAD_REQUEST, "닉네임은 2~20자 사이여야 합니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "닉네임 형식이 잘못되었습니다.");


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
