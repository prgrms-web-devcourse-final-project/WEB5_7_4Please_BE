package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBidInfo;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryBase;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementInfo;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Pageable pageable) {
        Long memberId = 2L;

        // 1. 기본 입찰 정보 조회
        Page<MyPageBidHistoryBase> basePage =
                bidRepository.findMyBidHistoryBase(memberId, pageable);

        if (basePage.isEmpty()) {
            return PageResponse.fromPage(Page.empty());
        }

        List<Long> auctionIds = basePage.getContent().stream()
                .map(MyPageBidHistoryBase::auctionId)
                .distinct()
                .toList();

        // 2. 결제/배송 상태 정보 조회
        Map<Long, SettlementInfo> settlementMap = bidRepository
                .findSettlementInfoByAuctionIds(memberId, auctionIds)
                .stream()
                .collect(Collectors.toMap(
                        SettlementInfo::auctionId,
                        info -> info
                ));

        // 3. 최고가 입찰 정보 조회
        Map<Long, HighestBidInfo> highestBidMap = bidRepository
                .findHighestBidInfoByAuctionIds(auctionIds)
                .stream()
                .collect(Collectors.toMap(
                        HighestBidInfo::auctionId,
                        info -> info
                ));

        // 4. 상태 결정
        List<MyPageBidHistory> result = basePage.getContent().stream()
                .map(base -> convertToMyPageBidHistory(base, settlementMap, highestBidMap))
                .toList();

        return PageResponse.fromPage(new PageImpl<>(result, pageable, basePage.getTotalElements()));
    }

    private MyPageBidHistory convertToMyPageBidHistory(
            MyPageBidHistoryBase base,
            Map<Long, SettlementInfo> settlementMap,
            Map<Long, HighestBidInfo> highestBidMap
    ) {

        SettlementInfo settlement = settlementMap.get(base.auctionId());
        HighestBidInfo highestBid = highestBidMap.get(base.auctionId());

        String status = determineStatus(base, settlement);

        String paymentDeadline = formatPaymentDeadline(settlement);

        return new MyPageBidHistory(
                base.auctionId(),
                base.bidId(),
                base.thumbnailUrl(),
                base.productName(),
                status,
                BigDecimal.valueOf(base.startingPrice()),
                BigDecimal.valueOf(highestBid != null ? highestBid.highestPrice() : 0.0),
                BigDecimal.valueOf(base.instantBidPrice()),
                BigDecimal.valueOf(base.bidPrice()),
                base.bidTime(),
                base.createdAt(),
                paymentDeadline,
                base.sellerNickName()
        );
    }

    private String determineStatus(MyPageBidHistoryBase base, SettlementInfo settlement) {
        if (base.auctionStatus().equals("OPEN")) {
            return "진행중";
        }

        if (settlement != null && settlement.settlementStatus() != null) {
            String settlementStatus = settlement.settlementStatus();
            String shipmentStatus = settlement.shipmentStatus();

            return switch (settlementStatus) {
                case "SUCCESS" -> switch (shipmentStatus) {
                    case "DELIVERED" -> "구매확정";
                    case "INTRANSIT" -> "배송중";
                    default -> "결제 완료";
                };
                case "PENDING" -> "낙찰";
                case "REJECTED" -> "결제 실패";
                default -> "문제 발생";
            };
        }

        return switch (base.auctionStatus()) {
            case "FAIL" -> "패찰";
            case "CLOSED" -> {
                boolean isSuccessful = Boolean.TRUE.equals(base.isSuccessfulBidder());
                if (isSuccessful) {
                    yield "낙찰";
                } else {
                    yield "경매 종료";
                }
            }
            default -> "진행중";
        };
    }

    private String formatPaymentDeadline(SettlementInfo settlement) {
        if (settlement != null && settlement.paymentDeadline() != null) {
            return settlement.paymentDeadline().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            );
        }
        return "";
    }
}
