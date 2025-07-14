package com.deal4u.fourplease.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다."),

    INVALID_AUTH_HEADER(HttpStatus.BAD_REQUEST, "Authorization 헤더 형식이 잘못되었습니다."),
    TOKEN_ALREADY_BLACKLISTED(HttpStatus.FORBIDDEN, "이미 블랙리스트 처리된 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰 타입입니다."),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "토큰 형식이 올바르지 않습니다."),
    TOKEN_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "토큰 타입을 찾을 수 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원되지 않는 토큰 형식입니다."),
    OAUTH_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "소셜 로그인으로부터 이메일을 받아올 수 없습니다."),


    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일의 회원이 존재하지 않습니다."),
    MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN, "탈퇴한 회원입니다."),

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
