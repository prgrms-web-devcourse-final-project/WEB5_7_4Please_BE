package com.deal4u.fourplease.domain.auction.dto;

import java.math.BigDecimal;
import java.util.List;

public record BidSummaryDto(
        BigDecimal maxPrice,
        int bidCount
) {
    public static BidSummaryDto toBidSummaryDto(List<Long> bidList) {
        if (bidList.isEmpty()) {
            return new BidSummaryDto(BigDecimal.ZERO, 0);
        }
        return new BidSummaryDto(BigDecimal.valueOf(bidList.getFirst()), bidList.size());
    }
}