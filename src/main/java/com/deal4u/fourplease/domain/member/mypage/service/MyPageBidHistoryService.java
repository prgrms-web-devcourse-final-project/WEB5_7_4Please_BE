package com.deal4u.fourplease.domain.member.mypage.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.MAX_PRICE_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.WINNIGBID_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageBidHistoryService {

    private final BidRepository bidRepository;
    private final SettlementRepository settlementRepository;

    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Pageable pageable) {
        Long memberId = 1L;  // 실제 로그인한 사용자 ID로 변경해야 함.

        // 내 입찰 내역 조회
        Page<Bid> myBidsPage =
                bidRepository.findByBidderMemberIdOrderByBidTimeDesc(memberId, pageable);

        Page<MyPageBidHistory> bidHistoryPage = myBidsPage.map(this::convertToBidHistory);

        return PageResponse.fromPage(bidHistoryPage);
    }

    private MyPageBidHistory convertToBidHistory(Bid bid) {
        Auction auction = bid.getAuction();

        // 상태 결정
        String status = determineStatus(auction, bid);

        // 최고 입찰가 계산
        BigDecimal maxPrice = getAuctionMaxprice(auction);

        // 최종 낙찰자 정보
        String finalBidderNickName = null;
        BigDecimal finalBidPrice = BigDecimal.ZERO;

        if (auction.getStatus() == AuctionStatus.CLOSED) {
            Bid successBid = getSuccessBid(auction);
            finalBidderNickName = successBid.getBidder().getMember().getNickName();
            finalBidPrice = successBid.getPrice();
        }

        // 결제 마감일 정보
        String paymentDeadline = getPaymentDeadline(auction, bid);

        return new MyPageBidHistory(
                auction.getAuctionId(),
                bid.getBidId(),
                auction.getProduct().getThumbnailUrl(),
                auction.getProduct().getName(),
                status,
                auction.getStartingPrice().longValue(),
                maxPrice,
                auction.getInstantBidPrice(),
                bid.getPrice().longValue(),
                finalBidderNickName,
                finalBidPrice.longValue(),
                bid.getBidTime(),
                bid.getCreatedAt(),
                paymentDeadline,
                auction.getProduct().getSeller().getMember().getNickName()
        );
    }

    private String determineStatus(Auction auction, Bid bid) {
        return switch (auction.getStatus()) {
            case FAIL -> "패찰";
            case OPEN -> "진행중";
            case CLOSED -> bid.isSuccessfulBidder() ? getSettlementStatus(auction, bid) : "경매 종료";
        };
    }

    private String getSettlementStatus(Auction auction, Bid bid) {
        return settlementRepository.findByAuctionAndBidder(auction, bid.getBidder())
                .map(settlement -> {
                    // settlement 상태와 settlementId 로그
                    log.info("Settlement found with ID: {} for status: {}",
                            settlement.getSettlementId(), settlement.getStatus());

                    return Map.of(
                            SettlementStatus.SUCCESS, "결제 완료",
                            SettlementStatus.PENDING, "결제 대기",
                            SettlementStatus.REJECTED, "결제 실패"
                    ).getOrDefault(settlement.getStatus(), "낙찰");
                })
                .orElseGet(() -> {
                    log.warn("No settlement found for auctionId: {} and bidderId: {}",
                            auction.getAuctionId(), bid.getBidder().getMember().getMemberId());
                    return "낙찰";
                });
    }


    private String getPaymentDeadline(Auction auction, Bid bid) {
        return settlementRepository.findByAuctionAndBidder(auction, bid.getBidder())
                .filter(settlement -> settlement.getPaymentDeadline() != null)
                .map(settlement -> settlement.getPaymentDeadline()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .orElse(null);
    }

    private Bid getSuccessBid(Auction auction) {
        return bidRepository.findSuccessfulBidByAuction(auction)
                .orElseThrow(WINNIGBID_NOT_FOUND::toException);
    }

    private BigDecimal getAuctionMaxprice(Auction auction) {
        return bidRepository.findMaxBidPriceByAuctionId(auction.getAuctionId())
                .orElseThrow(MAX_PRICE_NOT_FOUND::toException);
    }
}
