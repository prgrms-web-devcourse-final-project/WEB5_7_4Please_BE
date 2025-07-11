package com.deal4u.fourplease.domain.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionDetailResponse(

        BigDecimal highestBidPrice,

        BigDecimal instantBidPrice,

        Integer bidCount,

        BigDecimal startingPrice,

        String productName,

        Long categoryId,

        String categoryName,

        String description,

        LocalDateTime endTime,

        String thumbnailUrl,

        List<String> imageUrls

) {

}
