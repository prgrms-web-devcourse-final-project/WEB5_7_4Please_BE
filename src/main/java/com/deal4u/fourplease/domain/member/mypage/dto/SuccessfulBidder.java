package com.deal4u.fourplease.domain.member.mypage.dto;

public record SuccessfulBidder(
        Long auctionId,
        Long bidId,
        String nickname) {

}
