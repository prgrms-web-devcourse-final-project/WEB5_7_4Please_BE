package com.deal4u.fourplease.domain.notification.pushnotification.dto;

import java.util.List;

public record PushNotificationListResponse(
        List<PushNotificationResponse> pushNotificationResponses) {

}
