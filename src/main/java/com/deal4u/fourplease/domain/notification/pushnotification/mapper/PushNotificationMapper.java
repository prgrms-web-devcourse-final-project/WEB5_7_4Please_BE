package com.deal4u.fourplease.domain.notification.pushnotification.mapper;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotifications;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationMapper {

    public PushNotifications toPushNotifications(PushNotificationMessage pushNotificationMessage) {
        return new PushNotifications(pushNotificationMessage.getMemberIds(),
                pushNotificationMessage.getPushMessageBody().body());
    }
}
