package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageBidHistoryService {

    private final BidRepository bidRepository;
    private final SettlementRepository settlementRepository;

    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Pageable pageable) {

        Long memberId = 1L;

        Page<Bid> myBidsPage =
                bidRepository.findByBidderMemberIdOrderByBidTimeDesc(memberId, pageable);

        Page<MyPageBidHistory> bidHistoryPage = myBidsPage.map(this::convertToBidHistory);

        return PageResponse.fromPage(bidHistoryPage);
    }

    private MyPageBidHistory convertToBidHistory(Bid bid) {
        Auction auction = bid.getAuction();

        // 상태 결정 로직
        String status = determineStatus(auction, bid);

        // 해당 경매의 모든 입찰 정보 조회
        List<Bid> allBids = bidRepository.findByAuctionOrderByPriceDesc(auction);

        // 최고 입찰가 계산
        BigDecimal highestBidPrice = allBids.isEmpty() ?
                auction.getStartingPrice() :
                allBids.getFirst().getPrice();

        // 최종 낙찰자 정보
        String finalBidder = null;
        Long finalBidPrice = null;

        if (auction.getStatus() == AuctionStatus.CLOSED && !allBids.isEmpty()) {
            Bid winningBid = allBids.stream()
                    .filter(Bid::isSuccessfulBidder)
                    .findFirst()
                    .orElse(allBids.getFirst());

            finalBidder =
                    winningBid.getBidder().getMember().getNickName();
            finalBidPrice = winningBid.getPrice().longValue();
        }

        // 결제 마감일 정보
        String paymentDeadline = null;
        if (bid.isSuccessfulBidder()) {
            Optional<Settlement> settlement = settlementRepository.findByAuctionAndBidder(
                    auction, bid.getBidder());
            if (settlement.isPresent() && settlement.get().getPaymentDeadline() != null) {
                paymentDeadline = settlement.get().getPaymentDeadline()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        }

        return new MyPageBidHistory(
                auction.getAuctionId(),
                bid.getBidId(),
                auction.getProduct().getThumbnailUrl(),
                auction.getProduct().getName(),
                status,
                auction.getStartingPrice().longValue(),
                highestBidPrice,
                auction.getInstantBidPrice(),
                bid.getPrice().longValue(),
                finalBidder,
                finalBidPrice,
                bid.getBidTime(),
                bid.getCreatedAt(),
                paymentDeadline,
                allBids.size(),
                auction.getProduct().getSeller().getMember().getNickName()
        );
    }

    private String determineStatus(Auction auction, Bid bid) {
        // 패찰
        if (auction.getStatus() == AuctionStatus.FAIL) {
            return "패찰";
        }

        // 진행중
        if (auction.getStatus() == AuctionStatus.OPEN) {
            return "진행중";
        }

        // 경매 종료 (CLOSED)
        if (auction.getStatus() == AuctionStatus.CLOSED) {
            if (bid.isSuccessfulBidder())
            // 낙찰자인 경우 - 결제 상태 확인
            {
                return settlementRepository.findByAuctionAndBidder(auction, bid.getBidder())
                        .map(settlement -> Map.of(
                                SettlementStatus.SUCCESS, "결제 완료",
                                SettlementStatus.PENDING, "결제 대기",
                                SettlementStatus.REJECTED, "결제 실패"
                        ).getOrDefault(settlement.getStatus(), "낙찰"))
                        .orElse("낙찰");
            }
            return "경매 종료";
        }

        return "알 수 없음";
    }
}
