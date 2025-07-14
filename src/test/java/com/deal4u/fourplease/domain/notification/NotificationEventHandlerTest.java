package com.deal4u.fourplease.domain.notification;

import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import com.deal4u.fourplease.domain.notification.email.HtmlEmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class NotificationEventHandlerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @MockitoBean
    private HtmlEmailSender emailSender;

    @Test
    @DisplayName("이벤트를 발행하면 메일이 전송된다.")
    void whenEventIsPublishedThenEmailIsSent() {
        HtmlEmailMessage message = HtmlEmailMessage.builder()
                .templateName("test")
                .addData("email", "test@test.com")
                .addEmail("test@test.com")
                .build();

        publisher.publishEvent(message);

        Mockito.verify(emailSender, Mockito.times(1)).send(message);
    }

}