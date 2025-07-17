package com.deal4u.fourplease.domain.notification.pushnotification.dto;

import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class PushNotifications {

    private final List<Long> memberIds;
    private final Map<String, Object> body;

    public List<PushNotificationCreateCommand> toCreateCommand() {
        return memberIds.stream().map(id -> new PushNotificationCreateCommand(id, body)).toList();
    }

}
