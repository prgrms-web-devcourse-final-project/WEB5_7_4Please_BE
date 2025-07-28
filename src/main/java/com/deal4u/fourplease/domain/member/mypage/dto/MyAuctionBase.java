package com.deal4u.fourplease.domain.member.mypage.dto;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Category;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyAuctionBase(

        // Auction
        Long auctionId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal instantPrice,
        AuctionStatus status,

        // Product
        String name,
        String thumbnailUrl,
        Category category,

        // Bid
        Long bidId,
        String bidderName,
        BigDecimal successfulBidPrice,
        Integer bidCount,
        BigDecimal currentHighestBidPrice,

        // Settlement
        LocalDateTime paymentDeadline,

        // Shipment
        LocalDateTime createdAt

) {

}
