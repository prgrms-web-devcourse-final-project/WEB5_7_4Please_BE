package com.deal4u.fourplease.domain.notification.pushnotification.service;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotifications;
import com.deal4u.fourplease.domain.notification.pushnotification.mapper.PushNotificationMapper;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushNotificationMapper pushNotificationMapper;
    private final PushNotificationSaver saver;

    public void send(PushNotificationMessage pushMessage) {
        PushNotifications pushNotifications = pushNotificationMapper.toPushNotifications(
                pushMessage);
        saver.save(pushNotifications.toCreateCommand());
    }
}
