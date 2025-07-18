package com.deal4u.fourplease.domain.notification.pushnotification.dto;

import java.util.Map;

public record PushNotificationResponse(Long id, boolean isRead, String type,
                                       Map<String, Object> data) {

}
