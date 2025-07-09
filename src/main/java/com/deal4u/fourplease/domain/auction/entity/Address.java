package com.deal4u.fourplease.domain.auction.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String address,
        String detailAddress,
        String zipCode
) {

}
