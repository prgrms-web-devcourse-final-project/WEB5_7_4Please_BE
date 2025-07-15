package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.SaleAuctionStatus;
import java.math.BigDecimal;

public record SellerSaleListResponse(
        long auctionId,

        String thumbnailUrl,
        String name,

        BigDecimal maxPrice,
        BigDecimal startBidPrice,

        String description,

        int bidCount,

        String status
) {

    public static SellerSaleListResponse toSellerSaleListResponse(
            Auction auction,
            Product product,
            BidSummaryDto bidSummaryDto,
            SaleAuctionStatus status
    ) {
        return new SellerSaleListResponse(
                auction.getAuctionId(),
                product.getThumbnailUrl(),
                product.getName(),
                bidSummaryDto.maxPrice(),
                auction.getStartingPrice(),
                product.getDescription(),
                bidSummaryDto.bidCount(),
                status.toString()
        );
    }
}
