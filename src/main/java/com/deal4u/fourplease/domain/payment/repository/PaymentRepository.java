package com.deal4u.fourplease.domain.payment.repository;

import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(OrderId orderId);

    Optional<Payment> findByOrderId(OrderId orderId);
}
