package com.deal4u.fourplease.domain.auction.service;

import static org.junit.jupiter.api.Assertions.*;

import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTests {

    @InjectMocks
    private AuctionService auctionService;

    @Mock
    private AuctionRepository auctionRepository;

    @Test
    @DisplayName("경매를 등록할 수 있다")
    void auction_can_be_created() throws Exception {



    }

}