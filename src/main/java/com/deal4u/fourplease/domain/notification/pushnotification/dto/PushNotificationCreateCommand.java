package com.deal4u.fourplease.domain.notification.pushnotification.dto;

import java.util.Map;

public record PushNotificationCreateCommand(Long memberId, Map<String, Object> message) {

}
