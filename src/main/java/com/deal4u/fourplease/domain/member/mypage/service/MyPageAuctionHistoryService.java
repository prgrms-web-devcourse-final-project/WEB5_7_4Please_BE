package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.CountBid;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBid;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageAuctionHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementDeadline;
import com.deal4u.fourplease.domain.member.mypage.dto.SuccessfulBidder;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageAuctionHistoryService {

    private static final DateTimeFormatter PAYMENT_DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final SettlementRepository settlementRepository;

    public PageResponse<MyPageAuctionHistory> getMyAuctionHistory(Member member,
                                                                  Pageable pageable) {

        // 1. 경매 정보 조회
        Page<Tuple> tuples = auctionRepository.findAllAuctionHistoryByMemberId(member.getMemberId(),
                pageable);

        if (tuples.isEmpty()) {
            return PageResponse.fromPage(new PageImpl<>(List.of(), pageable, 0));
        }

        Set<Long> uniqueAuctionIds = tuples.getContent().stream()
                .map(t -> (Long) t.get("auctionId"))
                .collect(Collectors.toSet());

        List<Long> auctionIds = List.copyOf(uniqueAuctionIds);

        // 최고가 조회
        List<HighestBid> highestBidsForAuctionIds = bidRepository.findHighestBidsForAuctionIds(
                auctionIds);
        Map<Long, HighestBid> highestBidMap = highestBidsForAuctionIds.stream()
                .collect(Collectors.toMap(HighestBid::auctionId, highestBid -> highestBid));

        // 입찰수 조회
        List<CountBid> findCountBidsForAuctionIds = bidRepository.findCountBidsForAuctionIds(
                auctionIds);
        Map<Long, CountBid> bidCountMap = findCountBidsForAuctionIds.stream()
                .collect(Collectors.toMap(CountBid::auctionId, countBid -> countBid));

        // 낙찰자 조회
        List<SuccessfulBidder> successfulBidders = bidRepository.findSuccessfulBidderForAuctionIds(
                auctionIds);
        Map<Long, SuccessfulBidder> successfulBidderMap = successfulBidders.stream()
                .collect(Collectors.toMap(SuccessfulBidder::auctionId,
                        successfulBidder -> successfulBidder));

        // deadline 조회
        List<SettlementDeadline> settlementDeadlines = settlementRepository
                .findSettlementDeadlinesByAuctionIds(
                        auctionIds);
        Map<Long, SettlementDeadline> deadlineMap = settlementDeadlines.stream()
                .collect(Collectors.toMap(SettlementDeadline::auctionId, deadline -> deadline));

        List<MyPageAuctionHistory> auctionHistories = tuples.getContent().stream()
                .map(tuple -> {
                    Long auctionId = (Long) tuple.get("auctionId");
                    String thumbnailUrl = (String) tuple.get("thumbnailUrl");
                    Category category = (Category) tuple.get("category");
                    String name = (String) tuple.get("name");
                    BigDecimal instantPrice = (BigDecimal) tuple.get("instantBidPrice");
                    LocalDateTime endTime = (LocalDateTime) tuple.get("endTime");
                    String orderId = (String) tuple.get("orderId"); // 추가된 부분

                    SettlementDeadline settlementDeadline = deadlineMap.get(auctionId);

                    String paymentDeadline =
                            settlementDeadline != null && settlementDeadline.deadline() != null
                                    ?
                                    settlementDeadline.deadline().format(PAYMENT_DEADLINE_FORMAT)
                                    : "";

                    HighestBid highestBid = highestBidMap.get(auctionId);
                    BigDecimal maxPrice = (BigDecimal) highestBid.highestBid();

                    CountBid countBid = bidCountMap.get(auctionId);

                    BigDecimal bidCount = BigDecimal.valueOf(countBid.countBid());

                    SuccessfulBidder successfulBidder = successfulBidderMap.get(auctionId);

                    Long bidId = null;
                    String bidderName = null;
                    if (successfulBidder != null) {
                        bidId = successfulBidder.bidId();
                        bidderName =
                                (successfulBidder != null && successfulBidder.nickname() != null)
                                        ? successfulBidder.nickname() : "낙찰자 없음";
                    }

                    LocalDateTime createdAt = (LocalDateTime) tuple.get("startTime");
                    AuctionStatus status = (AuctionStatus) tuple.get("status");

                    return new MyPageAuctionHistory(
                            auctionId,
                            thumbnailUrl,
                            category,
                            name,
                            instantPrice,
                            endTime,
                            maxPrice,
                            bidCount,
                            bidId,
                            bidderName,
                            paymentDeadline,
                            createdAt,
                            status,
                            orderId
                    );
                })
                .toList();

        Page<MyPageAuctionHistory> resultPage =
                new PageImpl<>(auctionHistories, pageable, tuples.getTotalElements());

        return PageResponse.fromPage(resultPage);
    }
}
