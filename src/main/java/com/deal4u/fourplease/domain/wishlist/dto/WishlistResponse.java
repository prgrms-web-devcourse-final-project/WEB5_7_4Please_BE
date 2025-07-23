package com.deal4u.fourplease.domain.wishlist.dto;

import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistResponse(
        Long wishlistId,
        Long auctionId,
        String thumbnailUrl,
        String name,
        BigDecimal maxPrice,
        int bidCount,
        LocalDateTime createdAt
) {
    public static WishlistResponse toWishlistResponse(
            Wishlist wishlist,
            BidSummaryDto bidSummaryDto
    ) {
        Auction auction = wishlist.getAuction();
        Product product = auction.getProduct();
        return new WishlistResponse(
                wishlist.getWishlistId(),
                auction.getAuctionId(),
                product.getThumbnailUrl(),
                product.getName(),
                bidSummaryDto.maxPrice(),
                bidSummaryDto.bidCount(),
                wishlist.getCreatedAt()
        );
    }
}
