package com.deal4u.fourplease.domain.notification.pushnotification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PushNotificationTest {

    // 처음_생성시_알림은_클릭하지_않은_상태이다
    @Test
    void statusIsNotClickWhenCreated() {
        PushNotification pushNotification = PushNotification.builder().memberId(1L)
                .message(Map.of("message", "message")).build();

        assertThat(pushNotification.isClicked()).isFalse();
    }

    // 알림을_클릭시_클릭됨상태로_바뀐다
    @Test
    void statusUpdateToClickedWhenClickNotification() {
        PushNotification pushNotification = PushNotification.builder().memberId(1L)
                .message(Map.of("message", "message")).build();

        pushNotification.click();

        assertThat(pushNotification.isClicked()).isTrue();
    }
}