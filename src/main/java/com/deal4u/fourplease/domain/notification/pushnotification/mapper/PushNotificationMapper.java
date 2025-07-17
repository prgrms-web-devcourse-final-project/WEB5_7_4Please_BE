package com.deal4u.fourplease.domain.notification.pushnotification.mapper;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationCreateCommand;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushMessageBody;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PushNotificationMapper {

    public static List<PushNotificationCreateCommand> toCreateCommands(
            PushNotificationMessage pushNotificationMessage) {
        PushMessageBody pushMessageBody = pushNotificationMessage.getPushMessageBody();

        return pushNotificationMessage.getMemberIds().stream().map(
                id -> new PushNotificationCreateCommand(
                        id, pushMessageBody.body()
                )).toList();

    }
}
