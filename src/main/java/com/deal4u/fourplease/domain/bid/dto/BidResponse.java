package com.deal4u.fourplease.domain.bid.dto;

import java.time.LocalDateTime;

public record BidResponse(
        Long auctionId,
        Long bidId,
        Long memberId,
        String bidderName,
        int bidPrice,
        LocalDateTime bidTime,
        boolean isSuccessfulBidder) {

}
