package com.deal4u.fourplease.domain.auction.entity;

public enum AuctionStatus {
    OPEN,            // 경매 진행 중
    SUCCESS,         // 낙찰
    CLOSED,          // 경매 종료
    FAIL,            // 패찰
    PAYMENT_COMPLETED, // 결제 완료
    PAYMENT_FAILED,   // 결제 실패
    SHIPPING,        // 배송 중
    PURCHASE_CONFIRMED, // 구매 확정
}
