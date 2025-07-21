package com.deal4u.fourplease.domain.wishlist.service;

import com.deal4u.fourplease.domain.auction.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final AuctionService auctionService;

    @Transactional
    public Long save(WishlistCreateRequest request, Member member) {
        Auction auction = auctionService.getAuctionByAuctionId(request.auctionId());
        Wishlist wishlist = request.toEntity(member, auction);

        return wishlistRepository.save(wishlist).getWishlistId();
    }
}
