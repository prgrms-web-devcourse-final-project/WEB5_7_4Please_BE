package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionStatusService {

    // 경매 종료
    @Transactional
    public void closeAuction(Auction auction) {
        auction.close();
    }

    // 패찰 상태로 변경
    @Transactional
    public void failAuction(Auction auction) {
        auction.fail();
    }

    // 결제 성공 상태로 변경
    @Transactional
    public void markAuctionAsSuccess(Auction auction) {
        auction.markAsSuccess();
    }

    // 결제 대기 상태로 변경
    @Transactional
    public void markAuctionAsPending(Auction auction) {
        auction.markAsPending();
    }

    // 차상위 대기 상태로 변경
    @Transactional
    public void markAuctionAsRejected(Auction auction) {
        auction.markAsRejected();
    }

    // 배송 완료 상태로 변경
    @Transactional
    public void markAuctionAsInTransit(Auction auction) {
        auction.markAsInTransit();
    }

    // 구매 확정 상태로 변경
    @Transactional
    public void markAuctionAsDelivered(Auction auction) {
        auction.markAsDelivered();
    }
}
