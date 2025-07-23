package com.deal4u.fourplease.domain.notification.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    private static final String ENCODING_TYPE = "UTF-8";

    private final JavaMailSender mailSender;

    public void sendHtml(String subject, String[] emails, String bodyHtml)
            throws MailSendException {
        try {
            MimeMessage messageObject = toMessageObject(subject, emails, bodyHtml);
            mailSender.send(messageObject);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

    private MimeMessage toMessageObject(String subject, String[] emails, String bodyHtml)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODING_TYPE);
        helper.setTo(emails);
        helper.setSubject(subject);
        helper.setText(bodyHtml, true);
        return message;
    }
}
