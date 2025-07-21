package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WishlistCreateRequest(
        @Positive
        @NotNull(message = "경매 ID를 입력해 주세요.")
        Long auctionId
) {
    public Wishlist toEntity(
            Member member,
            Auction auction
    ) {
        return Wishlist.builder()
                .memberId(member.getMemberId())
                .auction(auction)
                .deleted(false)
                .build();
    }
}
