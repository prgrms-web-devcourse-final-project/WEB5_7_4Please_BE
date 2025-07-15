package com.deal4u.fourplease.domain.notification.platorm.repository;

import com.deal4u.fourplease.domain.notification.platorm.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

}
