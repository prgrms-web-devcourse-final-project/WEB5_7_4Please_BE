package com.deal4u.fourplease.domain.notification.pushnotification.mapper;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationCreateCommand;
import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationListResponse;
import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationResponse;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.PushNotification;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushMessageBody;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PushNotificationMapper {

    public static List<PushNotificationCreateCommand> toCreateCommands(
            PushNotificationMessage pushNotificationMessage) {
        PushMessageBody pushMessageBody = pushNotificationMessage.getPushMessageBody();

        return pushNotificationMessage.getReceiverIds().stream().map(
                id -> new PushNotificationCreateCommand(
                        id, pushNotificationMessage.getType(), pushMessageBody.body()
                )).toList();

    }

    public static PushNotificationListResponse toPushNotificationListResponse(
            List<PushNotification> pushNotifications) {
        return new PushNotificationListResponse(toPushNotificationResponses(pushNotifications));
    }

    public static PushNotificationResponse toPushNotificationResponse(
            PushNotification pushNotification) {
        return new PushNotificationResponse(
                pushNotification.getId(),
                pushNotification.isClicked(),
                pushNotification.getType(),
                pushNotification.getMessage()
        );
    }

    private static List<PushNotificationResponse> toPushNotificationResponses(
            List<PushNotification> pushNotifications) {
        return pushNotifications.stream().map(PushNotificationMapper::toPushNotificationResponse).toList();
    }
}
