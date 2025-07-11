package com.deal4u.fourplease.domain.bid.mapper;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.dto.BidMessageResponse;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.BidMessageStatus;
import com.deal4u.fourplease.domain.bid.entity.Bidder;

public final class BidMapper {

    private BidMapper() {
    }

    public static Bid toEntity(Auction auction, Bidder bidder, int price) {
        return Bid.createBid(auction, bidder, price);
    }

    public static BidResponse toResponse(Bid bid) {
        return new BidResponse(
                bid.getAuction().getAuctionId(),
                bid.getBidId(),
                bid.getBidder().getMember().getMemberId(),
                bid.getBidder().getMember().getNickName(),
                bid.getPrice().intValue(),
                bid.getBidTime(),
                bid.isSuccessfulBidder()
        );
    }

    public static BidMessageResponse toMessageResponse(Bid bid, BidMessageStatus bidMessageStatus) {
        return new BidMessageResponse(bidMessageStatus, toResponse(bid));
    }
}
