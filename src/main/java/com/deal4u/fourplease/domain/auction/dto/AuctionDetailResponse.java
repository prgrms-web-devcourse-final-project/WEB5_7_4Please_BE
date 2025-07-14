package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
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

    public static AuctionDetailResponse toAuctionDetailResponse(
            BidSummaryDto bidSummaryDto,
            Auction auction,
            List<String> productImageUrls
    ) {
        Product product = auction.getProduct();
        return new AuctionDetailResponse(
                bidSummaryDto.maxPrice(),
                auction.getInstantBidPrice(),
                bidSummaryDto.bidCount(),
                auction.getStartingPrice(),
                product.getName(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getDescription(),
                auction.getDuration().getEndTime(),
                product.getThumbnailUrl(),
                productImageUrls
        );
    }

}
