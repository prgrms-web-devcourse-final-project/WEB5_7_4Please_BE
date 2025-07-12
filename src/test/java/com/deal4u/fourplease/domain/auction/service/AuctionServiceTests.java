package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genAuctionCreateRequest;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genMember;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.dto.ProductImageListResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private BidRepository bidRepository;

    @Mock
    private ProductImageService productImageService;

    @Test
    @DisplayName("경매를 등록할 수 있다")
    void save_should_save_auction() throws Exception {

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

    @Test
    @DisplayName("auctionId로 특정 경매를 조회 후 AuctionDetailResponse를 반환한다")
    void getByAuctionId_should_return_AuctionDetailResponse() throws Exception {

        Long auctionId = 1L;

        List<Long> bidList = List.of(200_0000L, 150_0000L, 100_0000L);
        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);

        ProductImageListResponse productImageListResp = mock(ProductImageListResponse.class);
        List<String> productImageUrls = List.of(
                "http://example.com/image1.jpg", "http://example.com/image2.jpg"
        );

        when(bidRepository.findPricesByAuctionIdOrderByPriceDesc(auctionId)).thenReturn(bidList);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        when(productImageListResp.toProductImageUrlList()).thenReturn(productImageUrls);
        when(productImageService.getByProduct(product)).thenReturn(productImageListResp);

        AuctionDetailResponse actualResp = auctionService.getByAuctionId(auctionId);

        assertThat(actualResp.highestBidPrice()).isEqualTo(BigDecimal.valueOf(bidList.getFirst()));
        assertThat(actualResp.instantBidPrice()).isEqualTo(auction.getInstantBidPrice());
        assertThat(actualResp.bidCount()).isEqualTo(bidList.size());
        assertThat(actualResp.productName()).isEqualTo(product.getName());
        assertThat(actualResp.categoryId()).isEqualTo(product.getCategory().getCategoryId());
        assertThat(actualResp.categoryName()).isEqualTo(product.getCategory().getName());
        assertThat(actualResp.description()).isEqualTo(product.getDescription());
        assertThat(actualResp.endTime()).isEqualTo(auction.getDuration().getEndTime());
        assertThat(actualResp.thumbnailUrl()).isEqualTo(product.getThumbnailUrl());
        assertThat(actualResp.imageUrls().getFirst()).isEqualTo(productImageUrls.getFirst());
        assertThat(actualResp.imageUrls().getLast()).isEqualTo(productImageUrls.getLast());

    }

    @Test
    @DisplayName("존재하지 않는 auctionId로 조회를 시도하면 404 예외가 발생한다")
    void throws_when_try_to_get_if_auction_not_exist() throws Exception {

        Long auctionId = 1L;
        List<Long> bidList = List.of(200_0000L, 150_0000L, 100_0000L);

        when(bidRepository.findPricesByAuctionIdOrderByPriceDesc(auctionId)).thenReturn(bidList);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> {
                    auctionService.getByAuctionId(auctionId);
                }
        ).isInstanceOf(GlobalException.class)
                .hasMessage("경매를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("auctionId로 경매를 삭제한다")
    void deleteByAuctionId_should_soft_delete_auction_by_auction_id() throws Exception {

        Long auctionId = 1L;

        Auction auction = genAuctionCreateRequest().toEntity(genProduct());

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        auctionService.deleteByAuctionId(auctionId);

        verify(productService).deleteProduct(auction.getProduct());

        assertThat(auction.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 auctionId로 삭제를 시도하면 404 예외가 발생한다")
    void throws_when_try_to_delete_if_auction_not_exist() throws Exception {

        Long auctionId = 1L;

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> {
                    auctionService.deleteByAuctionId(auctionId);
                }
        ).isInstanceOf(GlobalException.class)
                .hasMessage("경매를 찾을 수 없습니다.");

    }

}