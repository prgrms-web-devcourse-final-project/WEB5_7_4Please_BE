package com.deal4u.fourplease.domain.notification.pushnotification.service;

import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotifications;
import com.deal4u.fourplease.domain.notification.pushnotification.mapper.PushNotificationMapper;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PushNotificationServiceTest {

    @Test
    @DisplayName("푸시알림 전송 플로우가 정상적으로 동작한다")
    void pushNotificationSendFlowRunsSuccessfully() {
        String message = "test message";
        PushNotificationMessage testMessage = PushNotificationMessage.simpleMessageBuilder()
                .addTo(1L)
                .addTo(2L)
                .message(message)
                .build();

        PushNotificationMapper mapper = Mockito.mock(PushNotificationMapper.class);
        PushNotifications pushNotifications = toPushNotifications(List.of(1L, 2L), message);
        when(mapper.toPushNotifications(testMessage)).thenReturn(pushNotifications);

        PushNotificationSaver saver = Mockito.mock(PushNotificationSaver.class);
        PushNotificationService pushNotificationService = new PushNotificationService(mapper,
                saver);

        pushNotificationService.send(testMessage);

        Mockito.verify(saver, Mockito.times(1)).save(pushNotifications.toCreateCommand());
    }

    private PushNotifications toPushNotifications(List<Long> memberIds, String messagebody) {
        return new PushNotifications(memberIds, messagebody);
    }
}