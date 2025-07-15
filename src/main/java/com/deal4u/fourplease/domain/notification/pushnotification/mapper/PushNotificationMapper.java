package com.deal4u.fourplease.domain.notification.pushnotification.mapper;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotifications;
import com.deal4u.fourplease.domain.notification.pushnotification.exception.PushNotificationMappingException;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushMessageBody;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PushNotifications toPushNotifications(PushNotificationMessage pushNotificationMessage) {
        try {
            String body = toJson(pushNotificationMessage.getPushMessageBody());
            return new PushNotifications(pushNotificationMessage.getMemberIds(), body);
        } catch (JsonProcessingException e) {
            throw new PushNotificationMappingException(e.getMessage(), e);
        }
    }

    private String toJson(PushMessageBody messageBody) throws JsonProcessingException {
        return objectMapper.writeValueAsString(messageBody.body());
    }
}
