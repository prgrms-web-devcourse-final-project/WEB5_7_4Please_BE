package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MyAuctionBase;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageAuctionHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageAuctionHistoryService {

    private static final DateTimeFormatter PAYMENT_DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuctionRepository auctionRepository;

    public PageResponse<MyPageAuctionHistory> getMyAuctionHistory(Member member,
            Pageable pageable) {
        // 1. 경매 정보 조회
        Page<MyAuctionBase> myAuctionsPage = auctionRepository.findMyAuctionHistory(
                member.getMemberId(), pageable);

        // 2. 경매 정보 Pagination
        return PageResponse.fromPage(myAuctionsPage.map(this::mapToMyPageAuctionHistory));
    }

    private MyPageAuctionHistory mapToMyPageAuctionHistory(MyAuctionBase myAuctionBase) {
        String paymentDeadline = formatPaymentDeadline(myAuctionBase.paymentDeadline());
        BigDecimal highestPrice = myAuctionBase.currentHighestBidPrice() != null
                ? myAuctionBase.currentHighestBidPrice() : BigDecimal.ZERO;

        return new MyPageAuctionHistory(
                myAuctionBase.auctionId(),
                myAuctionBase.thumbnailUrl(),
                myAuctionBase.category(),
                myAuctionBase.name(),
                highestPrice,
                myAuctionBase.instantPrice(),
                myAuctionBase.bidCount(),
                myAuctionBase.endTime(),
                myAuctionBase.bidderName(),
                paymentDeadline,
                myAuctionBase.createdAt(),
                myAuctionBase.status()
        );
    }

    private String formatPaymentDeadline(LocalDateTime paymentDeadline) {
        return paymentDeadline
                != null ? paymentDeadline.format(PAYMENT_DEADLINE_FORMAT) : "";
    }
}
