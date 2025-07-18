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

    private static final DateTimeFormatter PAYMENT_DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Long MEMBER_ID = 2L; // 실제 컨텍스트 홀더에서 꺼내기

    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Pageable pageable) {
        // 1. 기본 입찰 정보 조회
        Page<MyPageBidHistoryBase> basePage = findMyBidHistoryBase(pageable);

        if (basePage.isEmpty()) {
            return PageResponse.fromPage(Page.empty());
        }

        // 2. 경매 ID 목록 추출
        List<Long> auctionIds = extractAuctionIds(basePage.getContent());

        // 3. 결제/배송 상태 정보 조회
        Map<Long, SettlementInfo> settlementMap = findSettlementInfoMap(auctionIds);

        // 4. 최고가 입찰 정보 조회
        Map<Long, HighestBidInfo> highestBidMap = findHighestBidInfoMap(auctionIds);

        // 5. 최종 결과 변환
        List<MyPageBidHistory> result =
                convertToMyPageBidHistoryList(basePage.getContent(), settlementMap, highestBidMap);

        return PageResponse.fromPage(new PageImpl<>(result, pageable, basePage.getTotalElements()));
    }

    private Page<MyPageBidHistoryBase> findMyBidHistoryBase(Pageable pageable) {
        return bidRepository.findMyBidHistoryBase(MEMBER_ID, pageable);
    }

    private List<Long> extractAuctionIds(List<MyPageBidHistoryBase> bidHistories) {
        return bidHistories.stream().map(MyPageBidHistoryBase::auctionId).distinct().toList();
    }

    private Map<Long, SettlementInfo> findSettlementInfoMap(List<Long> auctionIds) {
        return bidRepository.findSettlementInfoByAuctionIds(MEMBER_ID, auctionIds).stream()
                .collect(Collectors.toMap(SettlementInfo::auctionId, settlement -> settlement));
    }

    private Map<Long, HighestBidInfo> findHighestBidInfoMap(List<Long> auctionIds) {
        return bidRepository.findHighestBidInfoByAuctionIds(auctionIds).stream()
                .collect(Collectors.toMap(HighestBidInfo::auctionId, bidInfo -> bidInfo));
    }

    private List<MyPageBidHistory> convertToMyPageBidHistoryList(
            List<MyPageBidHistoryBase> baseList, Map<Long, SettlementInfo> settlementMap,
            Map<Long, HighestBidInfo> highestBidMap) {

        return baseList.stream()
                .map(base -> convertToMyPageBidHistory(base, settlementMap, highestBidMap))
                .toList();
    }

    private MyPageBidHistory convertToMyPageBidHistory(MyPageBidHistoryBase base,
                                                       Map<Long, SettlementInfo> settlementMap,
                                                       Map<Long, HighestBidInfo> highestBidMap) {

        SettlementInfo settlement = settlementMap.get(base.auctionId());
        HighestBidInfo highestBid = highestBidMap.get(base.auctionId());

        String status = determineStatus(base, settlement);
        String paymentDeadline = formatPaymentDeadline(settlement);
        BigDecimal highestPrice = getHighestPrice(highestBid);

        return new MyPageBidHistory(base.auctionId(), base.bidId(), base.thumbnailUrl(),
                base.productName(), status, BigDecimal.valueOf(base.startingPrice()), highestPrice,
                BigDecimal.valueOf(base.instantBidPrice()), BigDecimal.valueOf(base.bidPrice()),
                base.bidTime(), base.createdAt(), paymentDeadline, base.sellerNickName());
    }

    private BigDecimal getHighestPrice(HighestBidInfo highestBid) {
        return BigDecimal.valueOf(highestBid != null ? highestBid.highestPrice() : 0.0);
    }

    private String determineStatus(MyPageBidHistoryBase base, SettlementInfo settlement) {
        // 경매가 진행 중인 경우
        if (isAuctionOpen(base)) {
            return "진행중";
        }

        // 결제/배송 정보가 있는 경우
        if (hasSettlementInfo(settlement)) {
            return determineStatusBySettlement(settlement);
        }

        // 경매 상태에 따른 처리
        return determineStatusByAuction(base);
    }

    private boolean isAuctionOpen(MyPageBidHistoryBase base) {
        return "OPEN".equals(base.auctionStatus());
    }

    private boolean hasSettlementInfo(SettlementInfo settlement) {
        return settlement != null && settlement.settlementStatus() != null;
    }

    private String determineStatusBySettlement(SettlementInfo settlement) {
        String settlementStatus = settlement.settlementStatus();
        String shipmentStatus = settlement.shipmentStatus();

        return switch (settlementStatus) {
            case "SUCCESS" -> determineStatusByShipment(shipmentStatus);
            case "PENDING" -> "낙찰";
            case "REJECTED" -> "결제 실패";
            default -> "문제 발생";
        };
    }

    private String determineStatusByShipment(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "DELIVERED" -> "구매확정";
            case "INTRANSIT" -> "배송중";
            default -> "결제 완료";
        };
    }

    private String determineStatusByAuction(MyPageBidHistoryBase base) {
        return switch (base.auctionStatus()) {
            case "FAIL" -> "패찰";
            case "CLOSED" -> determineClosedAuctionStatus(base);
            default -> "진행중";
        };
    }

    private String determineClosedAuctionStatus(MyPageBidHistoryBase base) {
        boolean isSuccessful = Boolean.TRUE.equals(base.isSuccessfulBidder());
        return isSuccessful ? "낙찰" : "경매 종료";
    }

    private String formatPaymentDeadline(SettlementInfo settlement) {
        if (settlement != null && settlement.paymentDeadline() != null) {
            return settlement.paymentDeadline().format(PAYMENT_DEADLINE_FORMAT);
        }
        return "";
    }
}
