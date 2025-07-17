package com.deal4u.fourplease.domain.notification.pushnotification.service;

import com.deal4u.fourplease.domain.notification.pushnotification.entity.PushNotification;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.Receiver;
import com.deal4u.fourplease.domain.notification.pushnotification.mapper.PushNotificationMapper;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.repository.PushNotificationRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushNotificationSaver saver;
    private final PushNotificationRepository pushNotificationRepository;

    public void send(PushNotificationMessage pushMessage) {
        saver.save(PushNotificationMapper.toCreateCommands(pushMessage));
    }

    @Transactional
    public void click(Receiver receiver, Long notificationId) {
        PushNotification pushNotification = pushNotificationRepository.findById(notificationId)
                .orElseThrow(ErrorCode.ENTITY_NOT_FOUND::toException);
        if (!pushNotification.isSameReceiver(receiver)) {
            throw ErrorCode.UNAUTHORIZED_RECEIVER.toException();
        }

        pushNotification.click();
    }
}
