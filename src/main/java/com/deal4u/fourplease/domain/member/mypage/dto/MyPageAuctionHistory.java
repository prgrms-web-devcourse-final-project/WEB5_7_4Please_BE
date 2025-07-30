package com.deal4u.fourplease.domain.member.mypage.dto;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Category;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyPageAuctionHistory(
        Long auctionId,
        String thumbnailUrl,
        Category category,
        String name,
        BigDecimal instantPrice,
        LocalDateTime endTime,
        BigDecimal maxPrice,
        BigDecimal bidCount,
        Long bidId,
        String bidderName,
        String paymentDeadline,
        LocalDateTime createdAt,
        AuctionStatus status,
        String orderId
) {

}
