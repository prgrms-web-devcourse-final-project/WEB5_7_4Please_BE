package com.deal4u.fourplease.domain.auction.reader;

import com.deal4u.fourplease.domain.auction.entity.Auction;

public interface AuctionReader {
    Auction getAuctionByAuctionId(Long auctionId);
}
