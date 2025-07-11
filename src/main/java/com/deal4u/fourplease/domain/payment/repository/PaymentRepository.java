package com.deal4u.fourplease.domain.payment.repository;

import com.deal4u.fourplease.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
