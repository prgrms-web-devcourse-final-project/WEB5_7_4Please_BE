package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.service.WishlistService;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Page<AuctionListResponse> getAuctionListResponses(
            Page<Auction> auctionPage,
            Member member
    ) {
        List<Long> auctionIds = auctionPage.getContent().stream()
                .map(Auction::getAuctionId)
                .toList();

        // IN 쿼리로 한번에 가져오기
        Map<Long, BidSummaryDto> bidSummaryDtoMap = bidService.getBidSummaryDtoMap(auctionIds);

        Set<Long> wishlistAuctionIds = member == null ? Set.of() :
                wishlistService.getWishlistAuctionIds(auctionIds, member.getMemberId());

        return auctionPage.map(auction -> {
            BidSummaryDto bidSummaryDto = bidSummaryDtoMap.get(auction.getAuctionId());
            boolean isWishlist = wishlistAuctionIds.contains(auction.getAuctionId());

            return AuctionListResponse.toAuctionListResponse(
                    auction,
                    bidSummaryDto,
                    isWishlist
            );
        });
    }

    public Page<SellerSaleListResponse> getSellerSaleListResponses(Page<Auction> auctionPage) {
        List<Long> auctionIds = auctionPage.getContent().stream()
                .map(Auction::getAuctionId)
                .toList();

        Map<Long, BidSummaryDto> bidSummaryDtoMap = bidService.getBidSummaryDtoMap(auctionIds);

        return auctionPage.map(auction -> {
            BidSummaryDto bidSummaryDto = bidSummaryDtoMap.get(auction.getAuctionId());
            return SellerSaleListResponse.toSellerSaleListResponse(auction, bidSummaryDto);
        });
    }
}
