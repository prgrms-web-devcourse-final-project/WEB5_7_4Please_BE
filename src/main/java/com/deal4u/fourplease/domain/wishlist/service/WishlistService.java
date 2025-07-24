package com.deal4u.fourplease.domain.wishlist.service;

import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.auction.service.AuctionSupportService;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final AuctionService auctionService;
    private final BidService bidService;

    @Transactional
    public Long save(WishlistCreateRequest request, Member member) {
        Auction auction = auctionService.getAuctionByAuctionId(request.auctionId());
        Wishlist wishlist = request.toEntity(member, auction);

        return wishlistRepository.save(wishlist).getWishlistId();
    }

    @Transactional
    public void deleteByWishlistId(Long wishlistId) {
        Wishlist targetWishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(ErrorCode.WISHLIST_NOT_FOUND::toException);

        targetWishlist.delete();
    }

    @Transactional(readOnly = true)
    public PageResponse<WishlistResponse> findAll(Pageable pageable, Member member) {
        Page<Wishlist> wishlistPage = wishlistRepository.findAll(pageable, member.getMemberId());

        Page<WishlistResponse> wishlistResponsePage = wishlistPage
                .map(wishlist -> {
                    BidSummaryDto bidSummaryDto =
                            bidService.getBidSummaryDto(
                                    wishlist.getAuction().getAuctionId()
                            );
                    return WishlistResponse.toWishlistResponse(wishlist, bidSummaryDto);
                });

        return PageResponse.fromPage(wishlistResponsePage);
    }
}
