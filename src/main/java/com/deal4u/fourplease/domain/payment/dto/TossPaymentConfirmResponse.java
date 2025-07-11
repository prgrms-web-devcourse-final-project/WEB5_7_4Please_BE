package com.deal4u.fourplease.domain.payment.dto;

public record TossPaymentConfirmResponse(
        String orderId,
        String paymentKey,
        String status,
        String requestedAt,
        String approvedAt,
        int totalAmount
) {
}
