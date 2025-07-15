package com.deal4u.fourplease.domain.notification.pushnotification.repository;

import com.deal4u.fourplease.domain.notification.pushnotification.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

}
