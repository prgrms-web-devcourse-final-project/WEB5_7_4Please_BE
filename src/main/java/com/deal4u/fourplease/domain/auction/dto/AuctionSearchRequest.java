package com.deal4u.fourplease.domain.auction.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record AuctionSearchRequest(
        @Min(0)
        @Max(100)
        int page,

        @Min(0)
        @Max(100)
        int size,

        String keyword,

        @Positive
        @Nullable
        Long categoryId,

        @Pattern(regexp = "latest|bids|timeout")
        String order
) {

}
