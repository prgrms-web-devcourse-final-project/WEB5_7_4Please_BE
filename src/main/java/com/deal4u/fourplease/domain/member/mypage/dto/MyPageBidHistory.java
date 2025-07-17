package com.deal4u.fourplease.domain.member.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
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
        String sellerName
) {
}
