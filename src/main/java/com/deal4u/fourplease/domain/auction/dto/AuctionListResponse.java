package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionListResponse(
        long auctionId,

        String thumbnailUrl,
        // TODO: Category 확인 필요
        CategoryDto category,
        String name,

        BigDecimal maxPrice,
        BigDecimal instantPrice,

        int bidCount,

        LocalDateTime endTime,

        boolean isWishlist
) {
    public static AuctionListResponse toAuctionListResponse(
            Auction auction,
            BidSummaryDto bidSummaryDto,
            // TODO: wishlist 개발 후 수정 필요
            boolean isWishlist
    ) {
        Product product = auction.getProduct();
        return new AuctionListResponse(
                auction.getAuctionId(),
                product.getThumbnailUrl(),
                CategoryDto.toCategoryDto(product.getCategory()),
                product.getName(),
                bidSummaryDto.maxPrice(),
                auction.getInstantBidPrice(),
                bidSummaryDto.bidCount(),
                auction.getDuration().getEndTime(),
                isWishlist
        );
    }
}
