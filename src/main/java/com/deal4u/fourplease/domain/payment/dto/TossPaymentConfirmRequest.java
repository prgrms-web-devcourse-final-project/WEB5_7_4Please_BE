package com.deal4u.fourplease.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TossPaymentConfirmRequest(
        @NotBlank(message = "paymentKey는 필수 입력값입니다.")
        String paymentKey,

        @NotBlank(message = "orderId는 필수 입력값입니다.")
        String orderId,

        @NotNull(message = "amount는 필수 입력값입니다.")
        Integer amount
) {
}
