package com.deal4u.fourplease.domain.wishlist.service;

import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTests {

    @InjectMocks
    private WishlistService wishlistService;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private AuctionService auctionService;

    @Test
    @DisplayName("위시리스트를 등록할 수 있다")
    void saveShouldSaveWishlist() {



    }

}