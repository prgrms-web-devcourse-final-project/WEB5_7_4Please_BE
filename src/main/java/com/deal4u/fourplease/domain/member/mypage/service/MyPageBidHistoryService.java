package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryComplete;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        Page<MyPageBidHistoryComplete> completePage =
                bidRepository.findMyBidHistoryComplete(MEMBER_ID, pageable);

        List<MyPageBidHistory> result = completePage.getContent().stream()
                .map(this::convertToMyPageBidHistory)
                .toList();

        return PageResponse.fromPage(
                new PageImpl<>(result, pageable, completePage.getTotalElements()));
    }

    private MyPageBidHistory convertToMyPageBidHistory(MyPageBidHistoryComplete complete) {
        String status = determineStatus(complete);
        String paymentDeadline = formatPaymentDeadline(complete.paymentDeadline());
        BigDecimal highestPrice =
                complete.highestPrice() != null ? complete.highestPrice() : BigDecimal.ZERO;

        return new MyPageBidHistory(
                complete.auctionId(),
                complete.bidId(),
                complete.thumbnailUrl(),
                complete.productName(),
                status,
                complete.startingPrice(),
                highestPrice,
                complete.instantBidPrice(),
                complete.bidPrice(),
                complete.bidTime(),
                complete.createdAt(),
                paymentDeadline,
                complete.sellerNickName()
        );
    }

    private String determineStatus(MyPageBidHistoryComplete complete) {
        // 경매가 진행 중인 경우
        if (AuctionStatus.OPEN == complete.auctionStatus()) {
            return "진행중";
        }

        // 결제/배송 정보가 있는 경우
        if (complete.settlementStatus() != null) {
            return determineStatusBySettlement(complete.settlementStatus(),
                    complete.shipmentStatus());
        }

        // 경매 상태에 따른 처리
        return determineStatusByAuction(complete.auctionStatus(), complete.isSuccessfulBidder());
    }

    private String determineStatusBySettlement(String settlementStatus, String shipmentStatus) {
        return switch (settlementStatus) {
            case "SUCCESS" -> determineStatusByShipment(shipmentStatus);
            case "PENDING" -> "낙찰";
            case "REJECTED" -> "결제 실패";
            default -> "문제 발생";
        };
    }

    private String determineStatusByShipment(String shipmentStatus) {
        if (shipmentStatus == null) {
            return "결제 완료";
        }
        return switch (shipmentStatus) {
            case "DELIVERED" -> "구매확정";
            case "INTRANSIT" -> "배송중";
            default -> "결제 완료";
        };
    }

    private String determineStatusByAuction(AuctionStatus auctionStatus,
            Boolean isSuccessfulBidder) {
        return switch (auctionStatus) {
            case FAIL -> "패찰";
            case CLOSE -> Boolean.TRUE.equals(isSuccessfulBidder) ? "낙찰" : "경매 종료";
            default -> "진행중";
        };
    }

    private String formatPaymentDeadline(LocalDateTime paymentDeadline) {
        return paymentDeadline
                != null ? paymentDeadline.format(PAYMENT_DEADLINE_FORMAT) : "";
    }
}
