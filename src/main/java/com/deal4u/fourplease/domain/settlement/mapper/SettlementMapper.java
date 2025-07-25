package com.deal4u.fourplease.domain.settlement.mapper;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;

public class SettlementMapper {

    public static Settlement toEntity(Auction auction, Bidder bidder, int days) {
        return Settlement.builder()
                .auction(auction)
                .bidder(bidder)
                .status(SettlementStatus.PENDING)
                .paymentDeadline(auction.getDuration().getEndTime().plusDays(days))
                .build();
    }

}
