package com.deal4u.fourplease.domain.notification.platorm.dto;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class PushNotifications {

    private final List<Long> memberIds;
    private final String body;

    public List<PushNotificationCreateCommand> toCreateCommand() {
        return memberIds.stream().map(id -> new PushNotificationCreateCommand(id, body)).toList();
    }

}
