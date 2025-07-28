package com.deal4u.fourplease.domain.member.mypage.dto;

import java.time.LocalDateTime;

public record SettlementDeadline(
        Long auctionId,
        LocalDateTime deadline
) {
}