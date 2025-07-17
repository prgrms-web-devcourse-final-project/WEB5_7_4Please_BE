package com.deal4u.fourplease.domain.member.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyPageBidHistory(
        Long auctionId,
        Long bidId,
        String thumbnailUrl,
        String productName,
        String statusDescription,
        BigDecimal startingPrice,
        BigDecimal highestBidPrice,
        BigDecimal instantBidPrice,
        BigDecimal bidPrice,
        String successfulBidderNickName,
        BigDecimal successfulBidPrice,
        LocalDateTime bidTime,
        LocalDateTime createdAt,
        String paymentDeadlineFormatted,
        String sellerNickName
) {
}
