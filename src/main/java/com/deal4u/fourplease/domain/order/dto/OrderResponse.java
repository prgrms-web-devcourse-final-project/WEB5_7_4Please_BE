package com.deal4u.fourplease.domain.order.dto;

public record OrderResponse(
        String imageUrl,
        String productName,
        Long price,
        String sellerName,
        String address,
        String addressDetail,
        String zipCode,
        String phone,
        String deliveryRequest,
        String recipient
) {
}
