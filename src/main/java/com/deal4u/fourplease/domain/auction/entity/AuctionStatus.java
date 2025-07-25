package com.deal4u.fourplease.domain.auction.entity;

public enum AuctionStatus {
    OPEN,            // 경매 진행 중
    FAIL,            // 유찰
    PENDING,         // 결제 대기
    SUCCESS,         // 결제 완료
    REJECTED,        // 차상위 대기
    INTRANSIT,       // 배송 중
    DELIVERED,       // 구매 확정
    CLOSE            // 경매 종료
}
