package com.deal4u.fourplease.domain.notification.platorm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import com.deal4u.fourplease.domain.notification.platorm.dto.PushNotificationCreateCommand;
import com.deal4u.fourplease.domain.notification.platorm.entity.PushNotification;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PushNotificationSaverTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PushNotificationSaver pushNotificationSaver;

    @Test
    void saver() {
        List<PushNotificationCreateCommand> notifications = List.of(new PushNotificationCreateCommand(1L, "test"),
                new PushNotificationCreateCommand(2L, "test"));

        pushNotificationSaver.save(notifications);

        List<PushNotification> savedPushNotifications = getPushNotifications();
        assertThat(savedPushNotifications).hasSize(notifications.size());
        assertThat(savedPushNotifications)
                .extracting(PushNotification::getMemberId, PushNotification::getMessage)
                .containsExactlyInAnyOrder(
                        tuple(1L, "test"),
                        tuple(2L, "test")
                );

    }

    private List<PushNotification> getPushNotifications() {
        return jdbcTemplate.query(
                "select id,member_id,message from push_notification",
                (rs, rowNum) -> {
                    long id = rs.getLong(1);
                    long memberId = rs.getLong(2);
                    String message = rs.getString(3);
                    PushNotification pushNotification = new PushNotification(memberId, message);
                    ReflectionTestUtils.setField(pushNotification, "id", id);
                    return pushNotification;
                });
    }
}