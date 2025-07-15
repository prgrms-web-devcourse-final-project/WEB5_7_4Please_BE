package com.deal4u.fourplease.domain.auction.dto;

import java.math.BigDecimal;

public record SellerSaleListResponse(
        String thumbnailUrl,
        String name,

        BigDecimal maxPrice,
        BigDecimal startBidPrice,

        String description,

        int salesCount,

        int bidCount,

        String status
) {

}
