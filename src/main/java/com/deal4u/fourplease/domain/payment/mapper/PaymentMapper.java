package com.deal4u.fourplease.domain.payment.mapper;

import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import com.deal4u.fourplease.domain.payment.entity.PaymentStatus;
import java.math.BigDecimal;

public class PaymentMapper {

    private PaymentMapper() {
        // 인스턴스화 방지
    }

    public static Payment toPayment(Order order,
                                    TossPaymentConfirmRequest tossPaymentConfirmRequest,
                                    TossPaymentConfirmResponse response) {
        return Payment.builder()
                .amount(BigDecimal.valueOf(tossPaymentConfirmRequest.amount()))
                .status(PaymentStatus.SUCCESS)
                .paymentKey(response.paymentKey())
                .orderId(order.getOrderId())
                .build();
    }
}
