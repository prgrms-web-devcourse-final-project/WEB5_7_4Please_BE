package com.deal4u.fourplease.domain.payment.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @NaturalId
    private String paymentKey;

    @Embedded
    private OrderId orderId;
}