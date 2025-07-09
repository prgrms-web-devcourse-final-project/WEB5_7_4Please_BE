package com.deal4u.fourplease.global.exception;


import com.deal4u.fourplease.global.util.StringUtils;
import lombok.Getter;
import org.springframework.http.HttpStatus;


public class GlobalException extends RuntimeException {

    @Getter
    private final HttpStatus status;
    private final String message;
    private final Object[] args;

    protected GlobalException(Throwable cause, HttpStatus status, String message) {
        super(message, cause);
        this.status = status;
        this.message = null;
        this.args = null;
    }

    protected GlobalException(Throwable cause, HttpStatus status, String message, Object... args) {
        super(message, cause);
        this.status = status;
        this.message = message;
        this.args = args;
    }

    protected GlobalException(HttpStatus status, String message, Object... args) {
        this(null, status, message, args);
    }

    protected GlobalException(HttpStatus status, String message) {
        this(null, status, message);
    }

    public String getMessage() {
        if (message == null) {
            return super.getMessage();
        }
        return StringUtils.formatMessage(message, args);
    }
}
