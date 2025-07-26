package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.wishlist.service.WishlistService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionSupportService {

    private final BidService bidService;
    private final WishlistService wishlistService;

    public Page<AuctionListResponse> getAuctionListResponses(Page<Auction> auctionPage) {
        List<Long> auctionIds = new ArrayList<>();
        auctionPage.getContent().forEach(auction -> {
            auctionIds.add(auction.getAuctionId());
        });

        // IN 쿼리로 한번에 가져오기
        Map<Long, BidSummaryDto> bidSummaryDtoMap = bidService.getBidSummaryDtoMap(auctionIds);

        return auctionPage.map(auction -> {
            BidSummaryDto bidSummaryDto = bidSummaryDtoMap.get(auction.getAuctionId());
            return AuctionListResponse.toAuctionListResponse(
                    auction,
                    bidSummaryDto,
                    wishlistService.isWishlist(auction)
            );
        });
    }
}
