package com.deal4u.fourplease.domain.order.dto;

public record OrderResponse(
        String address,
        String addressDetail,
        String zipCode,
        String phone,
        String deliveryRequest,
        String recipient
) {
}
