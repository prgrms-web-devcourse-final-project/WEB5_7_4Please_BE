package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genAuctionCreateRequest;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genAuctionList;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genMember;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
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
import java.util.Collections;
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
    void save_should_save_auction() {

        Member member = genMember();
        AuctionCreateRequest req = genAuctionCreateRequest();

        ProductCreateDto productDto = req.toProductCreateDto(member);
        Product product = genProduct();

        when(productService.save(productDto)).thenReturn(product);

        auctionService.save(req, member);

        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);

        verify(auctionRepository).save(auctionCaptor.capture());
        Auction auction = auctionCaptor.getValue();

        int bidPeriod = 3;

        assertThat(auction.getStartingPrice()).isEqualTo(req.startingPrice());
        assertThat(auction.getInstantBidPrice()).isEqualTo(req.buyNowPrice());
        assertThat(auction.getDuration().getStartTime()).isEqualTo(req.startDate());
        assertThat(auction.getDuration().getEndTime()).isEqualTo(
                req.startDate().plusDays(bidPeriod));
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.OPEN);
    }

    @Test
    @DisplayName("auctionId로 특정 경매를 조회 후 AuctionDetailResponse를 반환한다")
    void get_by_auctionId_should_return_auction_detail_response() {

        Long auctionId = 1L;

        BidSummaryDto bidSummaryDto = new BidSummaryDto(
                BigDecimal.valueOf(2000000L),
                3
        );

        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);

        ProductImageListResponse productImageListResponse = mock(ProductImageListResponse.class);
        List<String> productImageUrlList = List.of("http://example.com/image1.jpg",
                "http://example.com/image2.jpg");

        when(auctionSupportService.getBidSummaryDto(auctionId)).thenReturn(bidSummaryDto);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));
        when(productImageService.getByProduct(product)).thenReturn(productImageListResponse);
        when(productImageListResponse.toProductImageUrlList()).thenReturn(productImageUrlList);

        AuctionDetailResponse actualResp = auctionService.getByAuctionId(auctionId);

        assertThat(actualResp.highestBidPrice()).isEqualTo(bidSummaryDto.maxPrice());
        assertThat(actualResp.instantBidPrice()).isEqualTo(auction.getInstantBidPrice());
        assertThat(actualResp.bidCount()).isEqualTo(bidSummaryDto.bidCount());
        assertThat(actualResp.productName()).isEqualTo(product.getName());
        assertThat(actualResp.categoryId()).isEqualTo(product.getCategory().getCategoryId());
        assertThat(actualResp.categoryName()).isEqualTo(product.getCategory().getName());
        assertThat(actualResp.description()).isEqualTo(product.getDescription());
        assertThat(actualResp.endTime()).isEqualTo(auction.getDuration().getEndTime());
        assertThat(actualResp.thumbnailUrl()).isEqualTo(product.getThumbnailUrl());
        assertThat(actualResp.imageUrls().getFirst()).isEqualTo(productImageUrlList.getFirst());
        assertThat(actualResp.imageUrls().getLast()).isEqualTo(productImageUrlList.getLast());

    }

    @Test
    @DisplayName("존재하지 않는 auctionId로 조회를 시도하면 404 예외가 발생한다")
    void throws_when_try_to_get_if_auction_not_exist() {

        Long auctionId = 1L;

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            auctionService.getByAuctionId(auctionId);
        }).isInstanceOf(GlobalException.class).hasMessage("해당 경매를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("auctionId로 경매를 삭제한다")
    void delete_by_auction_id_should_soft_delete_auction_by_auction_id() {

        Long auctionId = 1L;

        Auction auction = genAuctionCreateRequest().toEntity(genProduct());

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        auctionService.deleteByAuctionId(auctionId);

        verify(productService).deleteProduct(auction.getProduct());

        assertThat(auction.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 auctionId로 삭제를 시도하면 404 예외가 발생한다")
    void throws_when_try_to_delete_if_auction_not_exist() {

        Long auctionId = 1L;

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            auctionService.deleteByAuctionId(auctionId);
        }).isInstanceOf(GlobalException.class).hasMessage("해당 경매를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("전체 경매 목록을 조회한다")
    void findAllShouldReturnAuctionList() throws Exception {
        List<Auction> auctionList = genAuctionList();
        when(auctionRepository.findAll()).thenReturn(auctionList);

        when(auctionSupportService.getAuctionListResponses(anyList()))
                .thenReturn(genAuctionListResponseList());
        List<AuctionListResponse> resp = auctionService.findAll();

        assertThat(resp).hasSize(3);
        assertThat(resp.get(0).name()).isEqualTo("목도리");
        assertThat(resp.get(0).maxPrice()).isEqualTo(new BigDecimal("200000"));
        assertThat(resp.get(1).name()).isEqualTo("축구공");
        assertThat(resp.get(1).maxPrice()).isEqualTo(new BigDecimal("10000000"));
        assertThat(resp.get(2).maxPrice()).isEqualTo(new BigDecimal("2000000"));
        assertThat(resp.get(2).name()).isEqualTo("칫솔");
    }

    @Test
    @DisplayName("경매 목록이 없을 경우 빈 리스트를 반환한다")
    void findAllShouldReturnEmptyListWhenNoAuctionsExist() {

        when(auctionRepository.findAll()).thenReturn(Collections.emptyList());

        List<AuctionListResponse> resp = auctionService.findAll();

        assertThat(resp).isNotNull();
        assertThat(resp).isEmpty();
    }

    @Test
    @DisplayName("판매자 id로 해당 판매자의 판매내역을 조회한다")
    void findSalesBySellerIdShouldReturnSellerSaleList() throws Exception {

        Long sellerId = 1L;

        List<Product> productList = genProductList();
        List<Long> productIdList = List.of(1L, 2L, 3L);
        List<Auction> auctionList = genAuctionList();

        when(productService.getProductListBySellerId(sellerId)).thenReturn(productList);
        when(auctionRepository.findAllByProductId(productIdList)).thenReturn(auctionList);

        when(auctionSupportService.getBidSummaryDto(anyLong()))
                // id 별로 다른 값 반환
                .thenAnswer(invocation -> {
                    Long auctionId = invocation.getArgument(0);
                    if (auctionId == 1L) {
                        return new BidSummaryDto(BigDecimal.valueOf(2000000), 5);
                    } else if (auctionId == 2L) {
                        return new BidSummaryDto(BigDecimal.valueOf(10000000), 20);
                    } else if (auctionId == 3L) {
                        return new BidSummaryDto(BigDecimal.valueOf(2000000), 20);
                    } else {
                        return new BidSummaryDto(BigDecimal.ZERO, 0);
                    }
                });
        when(auctionSupportService.getSaleAuctionStatus(any(Auction.class)))
                .thenReturn(SaleAuctionStatus.OPEN);

        List<SellerSaleListResponse> resp = auctionService.findSalesBySellerId(sellerId);

        assertThat(resp).isNotNull();
        assertThat(resp).hasSize(3);
        assertThat(resp.get(0).name()).isEqualTo(productList.get(0).getName());
        assertThat(resp.get(0).thumbnailUrl()).isEqualTo(productList.get(0).getThumbnailUrl());
        assertThat(resp.get(1).name()).isEqualTo(productList.get(1).getName());
        assertThat(resp.get(1).thumbnailUrl()).isEqualTo(productList.get(1).getThumbnailUrl());
        assertThat(resp.get(2).name()).isEqualTo(productList.get(2).getName());
        assertThat(resp.get(2).thumbnailUrl()).isEqualTo(productList.get(2).getThumbnailUrl());
    }

}