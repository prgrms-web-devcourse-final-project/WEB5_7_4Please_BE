package com.deal4u.fourplease.domain.notification.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HtmlEmailService {

    private final HtmlTemplateRenderer htmlTemplateRenderer;
    private final EmailSender mailSender;

    public void send(HtmlEmailMessage htmlEmailMessage) throws MailSendException {
        String bodyHtml = htmlTemplateRenderer.render(htmlEmailMessage.getHtmlFileName(),
                htmlEmailMessage.getData());
        mailSender.sendHtml(htmlEmailMessage.getSubject(), htmlEmailMessage.getEmails(), bodyHtml);
    }
}
