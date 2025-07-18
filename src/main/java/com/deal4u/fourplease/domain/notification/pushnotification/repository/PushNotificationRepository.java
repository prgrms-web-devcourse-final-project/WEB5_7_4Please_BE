package com.deal4u.fourplease.domain.notification.pushnotification.repository;

import com.deal4u.fourplease.domain.notification.pushnotification.entity.PushNotification;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.Receiver;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

    @Query("""
            SELECT p FROM PushNotification p
                        WHERE p.receiver = :receiver
                        AND p.clicked = false
            """)
    List<PushNotification> findAllByReceiverAndUnClicked(Receiver receiver, Pageable pageable);

    Slice<PushNotification> findAllByReceiver(Receiver receiver, Pageable pageable);
}
