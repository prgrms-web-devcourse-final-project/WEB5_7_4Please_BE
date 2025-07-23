package com.deal4u.fourplease.domain.auction.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SellerSaleSearchRequest(
        @Min(0)
        @Max(100)
        Integer page,

        @Min(0)
        @Max(100)
        Integer size
) {

    public SellerSaleSearchRequest {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 20;
        }
    }
}
