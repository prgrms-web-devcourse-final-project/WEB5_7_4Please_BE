package com.deal4u.fourplease.domain.payment.util;

import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_PAYMENT_AMOUNT;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_CONFIRMATION_FAILED;

import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import java.math.BigDecimal;

public class PaymentValidator {

    private PaymentValidator() {
    }

    public static void validateAmount(TossPaymentConfirmRequest tossPaymentConfirmRequest,
                                      Order order) {
        BigDecimal amountFromRequest = new BigDecimal(tossPaymentConfirmRequest.amount());
        if (order.getPrice().compareTo(amountFromRequest) != 0) {
            throw INVALID_PAYMENT_AMOUNT.toException();
        }
    }

    public static void validatePaymentSuccess(TossPaymentConfirmResponse response) {
        if (!"DONE".equals(response.status())) {
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        }
    }
}
