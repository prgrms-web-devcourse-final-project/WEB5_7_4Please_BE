package com.deal4u.fourplease.domain.member.mypage.dto;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyBidBase(

        // Auction
        Long auctionId,
        BigDecimal startingPrice,
        BigDecimal instantBidPrice,
        AuctionStatus status,

        // Product
        Seller seller,
        String productName,
        String thumbnailUrl,

        // Bid
        Long bidId,
        BigDecimal bidPrice,
        LocalDateTime bidTime,
        Boolean isSuccessfulBidder,

        // Settlement
        SettlementStatus settlementStatus,
        LocalDateTime paymentDeadline,

        // Shipment
        ShipmentStatus shipmentStatus,

        // 최고 입찰가
        BigDecimal highestPrice
) {

}
