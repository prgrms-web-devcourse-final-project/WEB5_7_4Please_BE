package com.deal4u.fourplease.domain.member.mypage.dto;

import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
import java.time.LocalDateTime;

public record SettlementInfo(
        Long auctionId,
        SettlementStatus settlementStatus,
        LocalDateTime paymentDeadline,
        ShipmentStatus shipmentStatus
) {
}
