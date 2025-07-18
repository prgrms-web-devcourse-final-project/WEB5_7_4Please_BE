package com.deal4u.fourplease.domain.member.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyPageBidHistory(
        Long auctionId,
        Long bidId,
        String thumbnailUrl,
        String product,
        String status,
        BigDecimal startBidPrice,
        BigDecimal highestBidPrice,
        BigDecimal instantBidPrice,
        BigDecimal myBidPrice,
        LocalDateTime bidTime,
        LocalDateTime createdAt,
        String paymentDeadline,
        String sellerNickName
) {
}

