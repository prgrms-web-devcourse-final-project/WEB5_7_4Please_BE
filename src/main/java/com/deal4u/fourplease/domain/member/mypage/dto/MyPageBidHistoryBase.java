package com.deal4u.fourplease.domain.member.mypage.dto;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyPageBidHistoryBase(
        Long auctionId,
        Long bidId,
        String thumbnailUrl,
        String productName,
        AuctionStatus auctionStatus,
        BigDecimal startingPrice,
        BigDecimal instantBidPrice,
        BigDecimal bidPrice,
        Boolean isSuccessfulBidder,
        LocalDateTime bidTime,
        LocalDateTime createdAt,
        String sellerNickName
) {
}
