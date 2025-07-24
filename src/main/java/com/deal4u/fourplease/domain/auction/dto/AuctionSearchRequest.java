package com.deal4u.fourplease.domain.auction.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record AuctionSearchRequest(
        @Min(0)
        @Max(100)
        Integer page,

        @Min(0)
        @Max(100)
        Integer size,

        String keyword,

        @Positive
        @Nullable
        Long categoryId,

        @Pattern(regexp = "latest|bids|timeout")
        String order

) {

    public AuctionSearchRequest {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 20;
        }

        if (keyword == null) {
            keyword = "";
        }

        if (order == null || order.isBlank()) {
            order = "latest";
        }
    }

}
