package com.deal4u.fourplease.domain.settlement.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.BID_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.SECOND_HIGHEST_BIDDER_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.SETTLEMENT_ALREADY_EXISTS;
import static com.deal4u.fourplease.global.exception.ErrorCode.SETTLEMENT_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.auction.service.AuctionStatusService;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.mapper.SettlementMapper;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import com.deal4u.fourplease.global.scheduler.FailedSettlementScheduleService;
import com.deal4u.fourplease.global.scheduler.SettlementScheduleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementStatusService settlementStatusService;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final SettlementScheduleService settlementScheduleService;
    private final FailedSettlementScheduleService scheduleService;
    private final SecondBidderNotifier secondBidderNotifier;
    private final AuctionStatusService auctionStatusService;
    private final HighestBidderNotifier highestBidderNotifier;

    @Transactional
    public void save(Long auctionId, int days) {
        // 1. 경매 검증 및 종료
        Auction auction = closeAuction(auctionId);

        // 2. 로그인 유저의 정보를 기반으로 입찰자 조회
        Bidder bidder = getBidder(auction);

        // 3. 입찰자가 존재하지 않는 경우 스케쥴러 등록하지 않고 종료
        // 해당 부분은 추후 고도화 시에 method 형식으로 수정할 수 있습니다.
        if (bidder == null) {
            auctionStatusService.failAuction(auction);
            return;
        }

        // 3. 정산 생성 및 스케쥴러 등록
        saveSettlement(auction, bidder, days);
    }

    private void saveSettlement(Auction auction, Bidder bidder, int days) {
        // 1. 정산 생성
        Settlement settlement = SettlementMapper.toEntity(auction, bidder, days);
        Settlement save = settlementRepository.save(settlement);

        // 2. 정산 스케쥴러 생성
        settlementScheduleService.scheduleSettlementClose(save.getSettlementId(),
                save.getPaymentDeadline());

        highestBidderNotifier.send(bidder, auction, save.getPaymentDeadline());
    }

    private Auction closeAuction(Long auctionId) {
        // 1. 경매 검증
        Auction auction = getAuction(auctionId);
        // 2. 경매 종료
        auctionStatusService.closeAuction(auction);
        return auction;
    }

    private Auction getAuction(Long auctionId) {
        return auctionRepository.findByAuctionIdAndDeletedFalseAndStatusOpen(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }

    private Bidder getBidder(Auction auction) {
        Optional<Bid> bid = bidRepository.findTopByAuctionOrderByPriceDescBidTimeAsc(auction);

        if (bid.isPresent()) {
            Bid updateBid = bid.get();
            updateBid.update(true);
            return updateBid.getBidder();
        }

        return null;
    }


    @Transactional
    public void offerSecondBidder(Long auctionId) {
        Bid secondHighestBid = getSecondHighestBidOrThrow(auctionId);
        Auction auction = secondHighestBid.getAuction();

        validateIfSettlementAlreadyExists(secondHighestBid);

        LocalDateTime paymentDeadline = LocalDateTime.now().plusHours(48);
        Settlement settlement =
                createSettlementForSecondBidder(secondHighestBid, auction, paymentDeadline);

        scheduleService.scheduleFailedSettlement(settlement.getSettlementId(), paymentDeadline);

        secondBidderNotifier.send(secondHighestBid, auction, paymentDeadline);
    }

    @Transactional
    public void handleFailedSettlement(Long settlementId) {
        Settlement settlement = getSettlementOrThrow(settlementId);
        settlementStatusService.markSettlementAsRejected(settlement,
                "결제 기한 내에 결제를 완료하지 않았습니다.");
        auctionStatusService.failAuction(settlement.getAuction());
    }

    public void changeSettlementSuccess(Auction auction) {
        Settlement settlement = getSettlementOrThrow(auction);
        settlementStatusService.markSettlementAsSuccess(settlement);
        auctionStatusService.markAuctionAsSuccess(auction);

        scheduleService.cancelFailedSettlement(settlement.getSettlementId());
    }

    public void changeSettlementFailure(Auction auction) {
        Settlement settlement = getSettlementOrThrow(auction);
        settlementStatusService.markSettlementAsRejected(settlement, "결제가 실패되었습니다.");

        scheduleService.cancelFailedSettlement(settlement.getSettlementId());
    }

    private Settlement getSettlementOrThrow(Auction auction) {
        return settlementRepository.findPendingSettlementByAuction(auction,
                        SettlementStatus.PENDING)
                .orElseThrow(SETTLEMENT_NOT_FOUND::toException);
    }

    private Settlement getSettlementOrThrow(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(SETTLEMENT_NOT_FOUND::toException);
    }

    private Settlement createSettlementForSecondBidder(Bid secondHighestBid, Auction auction,
            LocalDateTime paymentDeadline) {
        Settlement settlement = Settlement.builder()
                .auction(auction)
                .bidder(secondHighestBid.getBidder())
                .status(SettlementStatus.PENDING)
                .paymentDeadline(paymentDeadline)
                .build();

        return settlementRepository.save(settlement);
    }

    private Bid getSecondHighestBidOrThrow(Long auctionId) {
        List<Bid> topBids = bidRepository.findTop2ByAuctionId(auctionId);

        if (topBids.size() < 2) {
            throw SECOND_HIGHEST_BIDDER_NOT_FOUND.toException();
        }

        return topBids.get(1);
    }

    private void validateIfSettlementAlreadyExists(Bid secondHighestBid) {
        boolean settlementExists =
                settlementRepository.existsByAuctionAndBidder(secondHighestBid.getAuction(),
                        secondHighestBid.getBidder());
        if (settlementExists) {
            throw SETTLEMENT_ALREADY_EXISTS.toException();
        }
    }

    /**
     * 결제 기간 만료로 인한 정산 실패 처리를 수행합니다.
     *
     * @param settlementId 정산 ID.
     */
    @Transactional
    public void expireSettlement(Long settlementId) {
        // 1. 정산 정보 조회
        Settlement settlement = getSettlementOrThrow(settlementId);

        // 2. 정산 상태를 `REJECTED`로 변경
        settlementStatusService.markSettlementAsRejected(settlement, "결제 기간이 만료되어서 정산이 취소되었습니다.");
        auctionStatusService.markAuctionAsRejected(settlement.getAuction());

        // 3. 해당 입찰의 낙찰을 무효 처리
        invalidateBid(settlement);

        // 4. 차상위 입찰을 낙찰 처리
        awardSecondHighestBid(settlement.getAuction());
    }

    private void invalidateBid(Settlement settlement) {
        Bid bid = bidRepository.findByAuctionAndBidder(settlement.getAuction(),
                settlement.getBidder()).orElseThrow(BID_NOT_FOUND::toException);
        bid.update(false);
    }

    private void awardSecondHighestBid(Auction auction) {
        Bid bid = bidRepository.findSecondHighestBidByAuctionIdForSchedule(auction.getAuctionId())
                .orElseThrow(BID_NOT_FOUND::toException);
        bid.update(true);
    }
}
