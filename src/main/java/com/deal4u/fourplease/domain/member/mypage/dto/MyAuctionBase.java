package com.deal4u.fourplease.domain.member.mypage.dto;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
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
        BigDecimal successfulBidPrice,
        Long bidCount,
        BigDecimal currentHighestBidPrice,

        // Settlement
        SettlementStatus settlementStatus,
        LocalDateTime paymentDeadline,

        // Shipment
        ShipmentStatus shipmentStatus,
        LocalDateTime createdAt

) {

}
