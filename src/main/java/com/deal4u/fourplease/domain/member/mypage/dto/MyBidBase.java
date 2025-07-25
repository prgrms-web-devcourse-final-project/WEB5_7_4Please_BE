package com.deal4u.fourplease.domain.member.mypage.dto;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// 사용할 DTO
public record MyPageBidHistoryComplete(

        // Auction
        Long auctionId,
        BigDecimal startingPrice,
        BigDecimal instantBidPrice,

        // Product
        String thumbnailUrl,
        String productName,

        // 내 입찰 금액
        Long bidId,
        BigDecimal bidPrice,
        LocalDateTime bidTime,
        Boolean isSuccessfulBidder,

        // isSuccessfulBidder(true) = 결제대기, 결제완료, 배송중, 구매확정

        // isSuccessfulBidder(false) = 경매종료

        // OPEN

        // FAIL

        // CLOSE -> Auction Repository를 통해서 가져옴 (`MyBidBase`를 통해서 가져올 것)

        // ========= Mapper로 Dto 합치기 ====

        // Settlement settlement = settlementRepository.findByBidId();

        // settlement 를 조회하는 Request를 요청

        // PENDING -> Settlement Pending

        // SUCCESS -> Settlement Success

        // REJECTED -> '내 입찰에 대한 결제를 내가 거절함' -> 결제 거절

        // =========== Settlement가 Success인 경우

        // if ( settlement.status == success) {

        // shipmentRepository.findByAuctionId()

        // INTRANSIT -> 배송중

        // DELIVERED -> 구매 확정

        // MyPageMapper.toMyPageBidResponse()

        // Page.content<MyBidHistory>

        // 최고 입찰 금액
        // if나 switch로 분기
        BigDecimal highestPrice
) {
}
