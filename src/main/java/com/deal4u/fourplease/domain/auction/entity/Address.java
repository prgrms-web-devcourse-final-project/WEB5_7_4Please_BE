package com.deal4u.fourplease.domain.auction.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String address,
        String addressDetail,
        String zipCode
) {
    public static Address empty() {
        return new Address(null, null, null);
    }
}
