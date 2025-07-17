package com.deal4u.fourplease.domain.member.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyPageBidHistory(
        Long auctionId,
        Long bidId,
        String thumbnailUrl,
        String product,
        String status,
        Long startBidPrice,
        BigDecimal highestBidPrice,
        BigDecimal instantBidPrice,
        Long myBidPrice,
        String finalBidder,
        Long finalBidPrice,
        LocalDateTime bidTime,
        LocalDateTime createdAt,
        String paymentDeadline,
        Integer bidCount,
        String sellerName
) {
}
