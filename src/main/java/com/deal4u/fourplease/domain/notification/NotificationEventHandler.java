package com.deal4u.fourplease.domain.notification;

import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import com.deal4u.fourplease.domain.notification.email.HtmlEmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final HtmlEmailSender mailSender;

    @EventListener(HtmlEmailMessage.class)
    public void sendEmail(HtmlEmailMessage htmlEmailMessage) {
        try {
            mailSender.send(htmlEmailMessage);
        } catch (MailSendException e) {
            log.error(e.getMessage(), e);
        }
    }
}
