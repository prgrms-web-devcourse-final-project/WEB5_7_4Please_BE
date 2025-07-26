package com.deal4u.fourplease.domain.wishlist.service;

import static com.deal4u.fourplease.domain.wishlist.validator.Validator.validateMember;

import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.service.AuctionReaderImpl;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final AuctionReaderImpl auctionReaderImpl;
    private final BidService bidService;

    @Transactional
    public Long save(WishlistCreateRequest request, Member member) {
        Auction auction = auctionReaderImpl.getAuctionByAuctionId(request.auctionId());
        Wishlist wishlist = request.toEntity(member, auction);

        return wishlistRepository.save(wishlist).getWishlistId();
    }

    @Transactional
    public void deleteByWishlistId(Long wishlistId, Member member) {
        Wishlist targetWishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(ErrorCode.WISHLIST_NOT_FOUND::toException);

        validateMember(targetWishlist, member);

        targetWishlist.delete();
    }

    @Transactional(readOnly = true)
    public PageResponse<WishlistResponse> findAll(Pageable pageable, Member member) {
        Page<Wishlist> wishlistPage = wishlistRepository.findAll(pageable, member.getMemberId());

        Page<WishlistResponse> wishlistResponsePage = getWishlistResponses(wishlistPage);

        return PageResponse.fromPage(wishlistResponsePage);
    }

    private Page<WishlistResponse> getWishlistResponses(Page<Wishlist> wishlistPage) {
        List<Long> auctionIds = new ArrayList<>();
        wishlistPage.getContent().forEach(wishlist -> {
            Long auctionId = wishlist.getAuction().getAuctionId();
            auctionIds.add(auctionId);
        });

        Map<Long, BidSummaryDto> bidSummaryDtoMap = bidService.getBidSummaryDtoMap(auctionIds);
        return wishlistPage
                .map(wishlist -> {
                    BidSummaryDto bidSummaryDto =
                            bidSummaryDtoMap.get(wishlist.getAuction().getAuctionId());
                    return WishlistResponse.toWishlistResponse(wishlist, bidSummaryDto);
                });
    }

    // TODO: 멤버 일치 여부 확인 필요
    public boolean isWishlist(Auction auction) {
        return wishlistRepository.existsByAuctionAndDeletedFalse(auction);
    }
}
