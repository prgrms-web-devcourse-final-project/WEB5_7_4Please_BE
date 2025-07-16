package com.deal4u.fourplease.domain.settlement.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    public void chagneSettlementSucess(Auction auction) {
        Settlement settlement = getSettlementOrThrow(auction);
        settlement.updateStatus(SettlementStatus.SUCCESS, LocalDateTime.now(), null);
    }

    private Settlement getSettlementOrThrow(Auction auction) {
        return settlementRepository.findByAuction(auction)
                .orElseThrow(ErrorCode.SETTLEMENT_NOT_FOUND::toException);
    }
}
