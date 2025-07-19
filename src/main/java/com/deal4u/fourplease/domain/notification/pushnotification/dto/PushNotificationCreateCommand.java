package com.deal4u.fourplease.domain.notification.pushnotification.dto;

import java.util.Map;

public record PushNotificationCreateCommand(Long memberId, String type,
                                            Map<String, Object> message) {

}
