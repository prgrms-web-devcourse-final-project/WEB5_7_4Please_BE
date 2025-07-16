package com.deal4u.fourplease.domain.settlement.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.SECOND_HIGHEST_BIDDER_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.SETTLEMENT_ALREADY_EXISTS;
import static com.deal4u.fourplease.global.exception.ErrorCode.SETTLEMENT_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final BidRepository bidRepository;

    public void changeSettlementSuccess(Auction auction) {
        Settlement settlement = getSettlementOrThrow(auction);
        settlement.updateStatus(SettlementStatus.SUCCESS, LocalDateTime.now(), null);
    }

    @Transactional
    public void offerSecondBidder(Long auctionId) {
        Bid secondHighestBid = getSecondHighestBidOrThrow(auctionId);

        Auction auction = secondHighestBid.getAuction();

        validateIfSettlementAlreadyExists(secondHighestBid);

        createSettlementForSecondBidder(secondHighestBid, auction);

        // todo: 차상위에게 알람보내기 구현
    }

    @Transactional
    public void handleFailedSettlements() {
        Iterable<Settlement> pendingSettlements =
                settlementRepository.findByStatus(SettlementStatus.PENDING);

        for (Settlement settlement : pendingSettlements) {
            if (!settlement.getStatus().equals(SettlementStatus.SUCCESS)) {
                settlement.updateStatus(SettlementStatus.REJECTED, LocalDateTime.now(),
                        "차상위 입찰자가 결제를 하지 않았습니다.");
            }
        }
    }

    private void createSettlementForSecondBidder(Bid secondHighestBid, Auction auction) {
        Settlement settlement = Settlement.builder()
                .auction(auction)
                .bidder(secondHighestBid.getBidder())
                .status(SettlementStatus.PENDING)
                .paymentDeadline(LocalDateTime.now().plusHours(48))
                .build();

        settlementRepository.save(settlement);
    }

    private Settlement getSettlementOrThrow(Auction auction) {
        return settlementRepository.findByAuction(auction)
                .orElseThrow(SETTLEMENT_NOT_FOUND::toException);
    }

    private Bid getSecondHighestBidOrThrow(Long auctionId) {
        return bidRepository.findSecondHighestBidByAuctionId(auctionId)
                .orElseThrow((SECOND_HIGHEST_BIDDER_NOT_FOUND::toException));
    }

    private void validateIfSettlementAlreadyExists(Bid secondHighestBid) {
        boolean settlementExists =
                settlementRepository.existsByAuctionAndBidder(secondHighestBid.getAuction(),
                        secondHighestBid.getBidder());
        if (settlementExists) {
            throw SETTLEMENT_ALREADY_EXISTS.toException();
        }
    }
}
