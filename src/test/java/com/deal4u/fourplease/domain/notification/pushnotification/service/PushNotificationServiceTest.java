package com.deal4u.fourplease.domain.notification.pushnotification.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationCreateCommand;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.PushNotification;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.Receiver;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.repository.PushNotificationRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PushNotificationServiceTest {

    @Test
    @DisplayName("푸시알림 전송 플로우가 정상적으로 동작한다")
    void pushNotificationSendFlowRunsSuccessfully() {
        String message = "test message";
        PushNotificationMessage testMessage = PushNotificationMessage.simpleMessageBuilder()
                .addReceiver(1L)
                .addReceiver(2L)
                .message(message)
                .build();

        PushNotificationSaver saver = Mockito.mock(PushNotificationSaver.class);
        PushNotificationService pushNotificationService = new PushNotificationService(saver, null);

        pushNotificationService.send(testMessage);

        Mockito.verify(saver, Mockito.times(1)).save(List.of(
                new PushNotificationCreateCommand(
                        1L,
                        "아무 타입",
                        Map.of("message", message)
                ),
                new PushNotificationCreateCommand(
                        2L,
                        "아무 타입2",
                        Map.of("message", message)
                )
        ));
    }

    @Test
    @DisplayName("남의 알림을 클릭시 예외 발생")
    void throwsExceptionWhenUserAccessesOthersNotification() {

        PushNotificationSaver saver = Mockito.mock(PushNotificationSaver.class);
        PushNotificationRepository pushNotificationRepository = Mockito.mock(
                PushNotificationRepository.class);
        when(pushNotificationRepository.findById(1L)).thenReturn(
                Optional.of(PushNotification.builder()
                        .memberId(2L)
                        .message(Map.of("message", "test"))
                        .build())
        );
        PushNotificationService pushNotificationService = new PushNotificationService(saver,
                pushNotificationRepository);

        ThrowingCallable throwingCallable = () -> pushNotificationService.click(Receiver.of(1L),
                1L);
        assertThatThrownBy(
                throwingCallable
        ).isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.FORBIDDEN_RECEIVER.getMessage());
    }

    @Test
    @DisplayName("찾을 수 없는 알림인 경우 예외")
    void throwsExceptionWhenNotificationDoesNotExist() {

        PushNotificationSaver saver = Mockito.mock(PushNotificationSaver.class);
        PushNotificationRepository pushNotificationRepository = Mockito.mock(
                PushNotificationRepository.class);
        when(pushNotificationRepository.findById(1L)).thenReturn(
                Optional.empty()
        );
        PushNotificationService pushNotificationService = new PushNotificationService(saver,
                pushNotificationRepository);

        ThrowableAssert.ThrowingCallable callable = () -> pushNotificationService.click(
                Receiver.of(1L), 1L);
        assertThatThrownBy(
                callable
        ).isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.ENTITY_NOT_FOUND.getMessage());
    }
}