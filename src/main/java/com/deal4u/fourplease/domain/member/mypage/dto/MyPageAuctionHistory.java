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
        BigDecimal maxPrice,
        BigDecimal instantPrice,
        Integer bidCount,
        LocalDateTime endTime,
        String bidderName,
        String paymentDeadline,
        LocalDateTime createdAt,
        AuctionStatus status
) {

}
