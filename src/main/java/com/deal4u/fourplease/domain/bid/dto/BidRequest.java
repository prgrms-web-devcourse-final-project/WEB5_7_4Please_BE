package com.deal4u.fourplease.domain.bid.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BidRequest(
        @NotNull(message = "경매Id는 필수 입니다.")
        Long auctionId,
        @Positive(message = "입찰 금액은 0보다 커야 합니다.")
        int price) {

}
