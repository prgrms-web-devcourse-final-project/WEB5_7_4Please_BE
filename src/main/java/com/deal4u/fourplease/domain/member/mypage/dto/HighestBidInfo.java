package com.deal4u.fourplease.domain.member.mypage.dto;

import java.math.BigDecimal;

public record HighestBidInfo(
        Long auctionId,
        BigDecimal highestPrice
) {
}
