package com.deal4u.fourplease.domain.notification.pushnotification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationCreateCommand;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.PushNotification;
import com.deal4u.fourplease.domain.notification.pushnotification.repository.PushNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PushNotificationSaverTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PushNotificationSaver pushNotificationSaver;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JsonComponentModule jsonComponentModule;
    @Autowired
    private PushNotificationRepository pushNotificationRepository;

    @Test
    void saver() {
        List<PushNotificationCreateCommand> notifications = List.of(
                new PushNotificationCreateCommand(1L, Map.of("message", "test")),
                new PushNotificationCreateCommand(2L, Map.of("message", "test")));

        pushNotificationSaver.save(notifications);

        List<PushNotification> savedPushNotifications = getPushNotifications();
        assertThat(savedPushNotifications).hasSize(notifications.size());
        assertThat(savedPushNotifications)
                .extracting(PushNotification::getMemberId, PushNotification::getMessage)
                .containsExactlyInAnyOrder(
                        tuple(1L, Map.of("message", "test")),
                        tuple(2L, Map.of("message", "test"))
                );
    }

    private List<PushNotification> getPushNotifications() {
        return pushNotificationRepository.findAll();
    }
}