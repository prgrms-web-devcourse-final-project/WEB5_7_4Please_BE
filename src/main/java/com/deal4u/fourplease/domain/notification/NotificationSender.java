package com.deal4u.fourplease.domain.notification;

import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSender {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void send(HtmlEmailMessage htmlEmailMessage) {
        applicationEventPublisher.publishEvent(htmlEmailMessage);
    }
}
