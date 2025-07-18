package com.deal4u.fourplease.domain.member.mypage.dto;

import java.time.LocalDateTime;

public record MyPageBidHistoryBase(
        Long auctionId,
        Long bidId,
        String thumbnailUrl,
        String productName,
        String auctionStatus,
        Double startingPrice,
        Double instantBidPrice,
        Double bidPrice,
        Boolean isSuccessfulBidder,
        LocalDateTime bidTime,
        LocalDateTime createdAt,
        String sellerNickName
) {
}
