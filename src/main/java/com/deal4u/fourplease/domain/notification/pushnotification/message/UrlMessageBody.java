package com.deal4u.fourplease.domain.notification.pushnotification.message;

import java.net.URL;
import java.util.Map;

public class UrlMessageBody implements PushMessageBody {

    private final URL url;
    private final String message;

    public UrlMessageBody(URL url, String message) {
        this.url = url;
        this.message = message;
    }

    @Override
    public Map<String, Object> body() {
        return Map.of("url", url.toString(), "message", message);
    }
}
