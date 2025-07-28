package com.deal4u.fourplease.domain.auction.dto;

public record SellerInfoResponse(
        Long sellerId,
        String sellerNickname,
        Integer totalReviews,
        Double averageRating,
        Integer completedDeals,
        String createdAt
) {
}
