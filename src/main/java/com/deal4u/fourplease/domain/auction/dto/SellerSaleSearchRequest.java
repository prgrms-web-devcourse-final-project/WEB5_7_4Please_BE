package com.deal4u.fourplease.domain.auction.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SellerSaleSearchRequest(
        @Min(0)
        @Max(100)
        int page,

        @Min(0)
        @Max(100)
        int size
) {

}
