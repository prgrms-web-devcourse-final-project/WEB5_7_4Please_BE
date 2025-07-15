package com.deal4u.fourplease.domain.notification.platorm.service;

import com.deal4u.fourplease.domain.notification.platorm.dto.PushNotifications;
import com.deal4u.fourplease.domain.notification.platorm.mapper.PushNotificationMapper;
import com.deal4u.fourplease.domain.notification.platorm.message.PushNotificationMessage;
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
