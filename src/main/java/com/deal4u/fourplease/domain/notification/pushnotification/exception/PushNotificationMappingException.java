package com.deal4u.fourplease.domain.notification.pushnotification.exception;

public class PushNotificationMappingException extends RuntimeException {

    public PushNotificationMappingException(String message) {
        super(message);
    }

    public PushNotificationMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
