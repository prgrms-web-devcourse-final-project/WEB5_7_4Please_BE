package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.global.exception.ErrorCode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;

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
            size = 3;
        }

        if (order == null || order.isBlank()) {
            order = "latest";
        }
    }

}
