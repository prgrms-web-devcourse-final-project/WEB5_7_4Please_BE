package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.service.TestUtils.genAuctionCreateRequest;
import static com.deal4u.fourplease.domain.auction.service.TestUtils.genMember;
import static com.deal4u.fourplease.domain.auction.service.TestUtils.genProduct;
import static com.deal4u.fourplease.domain.auction.service.TestUtils.genProductCreateDto;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.BidPeriod;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTests {

    @InjectMocks
    private AuctionService auctionService;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductService productService;

    @Test
    @DisplayName("경매를 등록할 수 있다")
    void auction_can_be_created() throws Exception {

        Member member = genMember();
        AuctionCreateRequest req = genAuctionCreateRequest();

        int bidPeriod = 3;

        ProductCreateDto productDto = req.toProductCreateDto(member);
        Product product = genProduct();

        when(productService.save(productDto)).thenReturn(product);

        auctionService.save(req, member);

        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);

        verify(auctionRepository).save(auctionCaptor.capture());
        Auction auction = auctionCaptor.getValue();

        assertThat(auction.getStartingPrice()).isEqualTo(req.startingPrice());
        assertThat(auction.getInstantBidPrice()).isEqualTo(req.buyNowPrice());
        assertThat(auction.getDuration().getStartTime()).isEqualTo(req.startDate());
        assertThat(auction.getDuration().getEndTime())
                .isEqualTo(req.startDate().plusDays(bidPeriod));
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.OPEN);
    }


}