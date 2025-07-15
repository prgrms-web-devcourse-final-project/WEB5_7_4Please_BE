package com.deal4u.fourplease.domain.notification;

import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import com.deal4u.fourplease.domain.notification.email.HtmlEmailService;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.service.PushNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class NotificationEventHandlerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private HtmlEmailService emailSender;

    @MockitoBean
    private PushNotificationService pushNotificationService;

    @Test
    @DisplayName("이벤트를 발행하면 메일이 전송된다.")
    void whenEventIsPublishedThenEmailIsSent() {
        HtmlEmailMessage message = HtmlEmailMessage.builder()
                .templateName("test")
                .addData("email", "test@test.com")
                .subject("subject")
                .addEmail("test@test.com")
                .build();

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publishEvent(message);
        });


        Mockito.verify(emailSender, Mockito.times(1)).send(message);
    }

    @Test
    @DisplayName("이벤트를 발행하면 푸쉬 알림이 전송된다.")
    void whenEventIsPublishedThenPushNotificationIsSent() {
        PushNotificationMessage message = PushNotificationMessage.simpleMessageBuilder()
                .addTo(1L)
                .message("test")
                .build();

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publishEvent(message);
        });

        Mockito.verify(pushNotificationService, Mockito.times(1)).send(message);
    }

    @Test
    @DisplayName("롤백되면 이벤트를 발행하면 메일이 전송되지 않는다")
    void whenEventIsPublishedButTransactionRollsBackThenEmailIsNotSent() {
        HtmlEmailMessage message = HtmlEmailMessage.builder()
                .templateName("test")
                .addData("email", "test@test.com")
                .subject("subject")
                .addEmail("test@test.com")
                .build();

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publishEvent(message);
            status.setRollbackOnly();
        });


        Mockito.verify(emailSender, Mockito.times(0)).send(message);
    }

    @Test
    @DisplayName("롤백되면 이벤트를 발행하면 푸쉬 알림이 전송되지 않는다")
    void whenEventIsPublishedButTransactionRollsBackThenPushNotificationIsNotSent() {
        PushNotificationMessage message = PushNotificationMessage.simpleMessageBuilder()
                .addTo(1L)
                .message("test")
                .build();

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publishEvent(message);
            status.setRollbackOnly();
        });

        Mockito.verify(pushNotificationService, Mockito.times(0)).send(message);
    }

}