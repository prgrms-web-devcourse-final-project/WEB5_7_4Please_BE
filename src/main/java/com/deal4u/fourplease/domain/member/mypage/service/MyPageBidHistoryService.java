package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBid;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementInfo;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageBidHistoryService {

    private static final DateTimeFormatter PAYMENT_DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Member member, Pageable pageable) {


        Page<Tuple> tuples =
                bidRepository.findAllBidHistoryByMemberId(member.getMemberId(), pageable);

        Set<Long> uniqueAuctionIds = tuples.getContent().stream()
                .map(t -> (Long) t.get("auctionId"))
                .collect(Collectors.toSet());

        if (uniqueAuctionIds.isEmpty()) {
            return PageResponse.fromPage(new PageImpl<>(List.of(), pageable, 0));
        }

        List<Long> auctionIds = List.copyOf(uniqueAuctionIds);

        // 3. 경매 정보 조회
        List<Auction> auctions = auctionRepository.findByAuctionIdIn(auctionIds);
        Map<Long, Auction> auctionMap = auctions.stream()
                .collect(Collectors.toMap(Auction::getAuctionId, auction -> auction));

        // 4. 최고가 정보 조회
        List<HighestBid> highestBids = bidRepository.findHighestBidsForAuctionIds(auctionIds);
        Map<Long, BigDecimal> highestBidMap = highestBids.stream()
                .collect(Collectors.toMap(HighestBid::auctionId, HighestBid::highestBid));

        // 5. 결제/배송 정보 조회
        List<SettlementInfo> settlementInfos =
                bidRepository.findSettlementInfoByAuctionIds(member.getMemberId(), auctionIds);
        Map<Long, SettlementInfo> settlementMap = settlementInfos.stream()
                .collect(Collectors.toMap(SettlementInfo::auctionId, info -> info));

        List<MyPageBidHistory> bidHistories = tuples.getContent().stream()
                .map(tuple -> {
                    Long auctionId = (Long) tuple.get("auctionId");
                    LocalDateTime bidTime = (LocalDateTime) tuple.get("bidTime");
                    Long bidId = (Long) tuple.get("bidId");
                    BigDecimal myBidPrice = (BigDecimal) tuple.get("myBidPrice");

                    Auction auction = auctionMap.get(auctionId);
                    if (auction == null) {
                        throw new IllegalArgumentException(
                                "Auction not found for ID: " + auctionId);
                    }

                    BigDecimal highestPrice =
                            highestBidMap.getOrDefault(auctionId, BigDecimal.ZERO);
                    SettlementInfo settlementInfo = settlementMap.get(auctionId);

                    String displayStatus =
                            determineDisplayStatus(auction, settlementInfo);

                    String paymentDeadline =
                            settlementInfo != null && settlementInfo.paymentDeadline() != null
                                    ?
                                    settlementInfo.paymentDeadline().format(PAYMENT_DEADLINE_FORMAT)
                                    : "";

                    return new MyPageBidHistory(
                            auction.getAuctionId(),
                            bidId,  // 실제 bidId 사용
                            auction.getProduct().getThumbnailUrl(),
                            auction.getProduct().getName(),
                            displayStatus,
                            auction.getStartingPrice(),
                            highestPrice,
                            auction.getInstantBidPrice(),
                            myBidPrice,
                            bidTime,
                            paymentDeadline,
                            auction.getProduct().getSeller() != null &&
                                    auction.getProduct().getSeller().getMember() != null
                                    ? auction.getProduct().getSeller().getMember().getNickName()
                                    : "알 수 없음"
                    );
                })
                .toList();

        Page<MyPageBidHistory> resultPage =
                new PageImpl<>(bidHistories, pageable, tuples.getTotalElements());
        return PageResponse.fromPage(resultPage);
    }

    private String determineDisplayStatus(Auction auction, SettlementInfo settlementInfo) {
        AuctionStatus auctionStatus = auction.getStatus();

        return switch (auctionStatus) {
            case OPEN -> "OPEN";
            case CLOSE ->
                    settlementInfo == null ? "FAIL" : determineStatusBySettlement(settlementInfo);
            case FAIL -> "FAIL";
            default -> "문제";
        };
    }

    private String determineStatusBySettlement(SettlementInfo settlementInfo) {
        SettlementStatus settlementStatus = settlementInfo.settlementStatus();

        return switch (settlementStatus) {
            case PENDING -> "PENDING";
            case SUCCESS -> determineStatusByShipment(settlementInfo.shipmentStatus());
            case REJECTED -> "REJECTED";
        };
    }

    private String determineStatusByShipment(ShipmentStatus shipmentStatus) {
        if (shipmentStatus == null) {
            return "SUCCESS";
        }

        return switch (shipmentStatus) {
            case INTRANSIT -> "INTRANSIT"; // 배송 중
            case DELIVERED -> "DELIVERED"; // 구매 확정
        };
    }
}
