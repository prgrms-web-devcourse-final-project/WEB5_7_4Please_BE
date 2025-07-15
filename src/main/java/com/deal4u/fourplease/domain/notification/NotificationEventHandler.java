package com.deal4u.fourplease.domain.notification;

import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import com.deal4u.fourplease.domain.notification.email.HtmlEmailService;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.exception.PushNotificationMappingException;
import com.deal4u.fourplease.domain.notification.pushnotification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final HtmlEmailService mailSender;
    private final PushNotificationService pushNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPushNotification(HtmlEmailMessage htmlEmailMessage) {
        try {
            mailSender.send(htmlEmailMessage);
        } catch (MailSendException e) {
            log.error(e.getMessage(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPushNotification(PushNotificationMessage pushNotificationMessage) {
        try {
            pushNotificationService.send(pushNotificationMessage);
        } catch (PushNotificationMappingException e) {
            log.error(e.getMessage(), e);
        }
    }
}
