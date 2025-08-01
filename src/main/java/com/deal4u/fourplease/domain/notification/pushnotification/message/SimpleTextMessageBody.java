package com.deal4u.fourplease.domain.notification.pushnotification.message;

import java.util.Map;

public class SimpleTextMessageBody implements PushMessageBody {

    private final String message;

    public SimpleTextMessageBody(String message) {
        this.message = message;
    }

    @Override
    public Map<String, Object> body() {
        return Map.of("message", message);
    }
}
