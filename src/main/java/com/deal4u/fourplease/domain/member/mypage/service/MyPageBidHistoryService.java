package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MyBidBase;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageBidHistoryService {

    private static final DateTimeFormatter PAYMENT_DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Member member, Pageable pageable) {
        // 1. 입찰 정보 조회
        Page<MyBidBase> myBidsPage = bidRepository.findMyBidHistory(member.getMemberId(), pageable);

        // 2. 입찰 정보 Pagination
        return PageResponse.fromPage(myBidsPage.map(this::mapToMyPageBidHistory));
    }

    private MyPageBidHistory mapToMyPageBidHistory(MyBidBase myBidBase) {
        String status = determineDisplayStatus(myBidBase);
        String paymentDeadline = formatPaymentDeadline(myBidBase.paymentDeadline());
        BigDecimal highestPrice =
                myBidBase.highestPrice() != null ? myBidBase.highestPrice() : BigDecimal.ZERO;

        return new MyPageBidHistory(
                myBidBase.auctionId(),
                myBidBase.bidId(),
                myBidBase.thumbnailUrl(),
                myBidBase.productName(),
                status,
                myBidBase.startingPrice(),
                highestPrice,
                myBidBase.instantBidPrice(),
                myBidBase.bidPrice(),
                myBidBase.bidTime(),
                paymentDeadline,
                // 탈퇴 유저를 고려하여서 우선 기입하였습니다.
                myBidBase.seller() != null && myBidBase.seller().getMember()
                        != null ? myBidBase.seller().getMember().getNickName() : "알 수 없음"
        );
    }

    private String determineDisplayStatus(MyBidBase myBidBase) {
        String currentStatus;
        switch (myBidBase.status()) {
            case AuctionStatus.OPEN:
                currentStatus = AuctionStatus.OPEN.name();
                break;
            case AuctionStatus.FAIL:
                currentStatus = AuctionStatus.FAIL.name();
                break;
            case AuctionStatus.CLOSE:
                if (myBidBase.isSuccessfulBidder()) {
                    // 낙찰
                    switch (myBidBase.settlementStatus()) {
                        case SettlementStatus.PENDING:
                            currentStatus = "PENDING";
                            break;
                        case SettlementStatus.SUCCESS:
                            if (myBidBase.shipmentStatus() == null) {
                                currentStatus = "SUCCESS";
                            }
                            switch (myBidBase.shipmentStatus()) {
                                case ShipmentStatus.INTRANSIT:
                                    currentStatus = "INTRANSIT";
                                    break;
                                case ShipmentStatus.DELIVERED:
                                    currentStatus = "DELIVERED";
                                    break;
                                default:
                                    currentStatus = "SUCCESS";
                                    break;
                            }
                            break;
                        case SettlementStatus.REJECTED:
                            currentStatus = "REJECTED";
                            break;
                        // 해당 default를 지나가는 경우는 존재하지 않음.
                        default:
                            currentStatus = "FAIL";
                            break;
                    }
                } else {
                    currentStatus = "FAIL";
                }
                break;
            default:
                currentStatus = "FAIL";
                break;
        }
        return currentStatus;
    }

    private String formatPaymentDeadline(LocalDateTime paymentDeadline) {
        return paymentDeadline
                != null ? paymentDeadline.format(PAYMENT_DEADLINE_FORMAT) : "";
    }
    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Pageable pageable) {
        return null;
    }
}