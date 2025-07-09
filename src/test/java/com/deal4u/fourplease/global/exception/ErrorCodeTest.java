package com.deal4u.fourplease.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorCodeTest {

    @Test
    @DisplayName("`ErrorCode`에서 매개변수를 받지 않는 경우")
    void entity_not_found_errorcode_check() {
        GlobalException exception = ErrorCode.ENTITY_NOT_FOUND.toException();
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getStatus());
        assertThat(exception.getMessage()).isEqualTo("엔티티를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("`ErrorCode`에서 메시지와 객체를 매개변수로 받는 경우")
    void errorcode_toexception_with_message_object() {
        GlobalException exception = ErrorCode.ENTITY_NOT_FOUND.toException(
                "{} 해당 Entity를 찾을 수 없습니다.", "userName");
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getStatus());
        assertThat(exception.getMessage()).isEqualTo("userName 해당 Entity를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("`ErrorCode`에서 메시지를 매개변수로 받는 경우")
    void errorcode_toexception_with_message() {
        GlobalException exception = ErrorCode.ENTITY_NOT_FOUND.toException("Entity가 존재하지 않습니다.");
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getStatus());
        assertThat(exception.getMessage()).isEqualTo("Entity가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("`ErrorCode`에서 원인을 매개변수로 받는 경우")
    void errorcode_toexception_with_cause() {
        String causeMessage = "RuntimeException Message";
        Throwable cause = new RuntimeException(causeMessage);

        GlobalException exception = ErrorCode.ENTITY_NOT_FOUND.toException(cause);

        assertThat(exception.getMessage()).isEqualTo("엔티티를 찾을 수 없습니다.");
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getStatus());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo(causeMessage);
    }

    @Test
    @DisplayName("`ErrorCode`에서 원인과 메시지를 매개변수로 받는 경우")
    void errorcode_toexception_with_cause_message() {
        String causeMessage = "RuntimeException Message";
        Throwable cause = new RuntimeException(causeMessage);

        GlobalException exception = ErrorCode.ENTITY_NOT_FOUND.toException(cause,
                "Entity가 존재하지 않습니다.");

        assertThat(exception.getStatus()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getStatus());
        assertThat(exception.getMessage()).isEqualTo("Entity가 존재하지 않습니다.");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo(causeMessage);
    }

    @Test
    @DisplayName("`ErrorCode`에서 원인, 메시지, 그리고 객체를 매개변수로 받는 경우")
    void errorcode_toexception_with_cause_message_object() {
        String causeMessage = "RuntimeException Message";
        Throwable cause = new RuntimeException(causeMessage);

        GlobalException exception = ErrorCode.ENTITY_NOT_FOUND.toException(cause,
                "{},{} 해당 Entity를 찾을 수 없습니다.", "userName", "userName2");
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getStatus());
        assertThat(exception.getMessage()).isEqualTo("userName,userName2 해당 Entity를 찾을 수 없습니다.");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo(causeMessage);
    }
}