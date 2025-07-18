package com.deal4u.fourplease.domain.member.mypage.dto;

import java.time.LocalDateTime;

public record SettlementInfo(
        Long auctionId,
        String settlementStatus,
        LocalDateTime paymentDeadline,
        String shipmentStatus
) {
}
