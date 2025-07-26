package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.reader.AuctionReader;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuctionReaderImpl implements AuctionReader {

    private final AuctionRepository auctionRepository;

    @Override
    @Transactional(readOnly = true)
    public Auction getAuctionByAuctionId(Long auctionId) {
        return auctionRepository.findByIdWithProduct(auctionId)
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);
    }
}
