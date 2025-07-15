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
    void saveShouldSaveAuction() throws Exception {

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
    void getByAuctionIdShouldReturnAuctionDetailResponse() throws Exception {

        Long auctionId = 1L;

        List<BigDecimal> bidList = List.of(BigDecimal.valueOf(2000000L),
                BigDecimal.valueOf(1500000L), BigDecimal.valueOf(1000000L));
        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);

        ProductImageListResponse productImageListResp = mock(ProductImageListResponse.class);
        List<String> productImageUrls = List.of("http://example.com/image1.jpg",
                "http://example.com/image2.jpg");

        when(bidRepository.findPricesByAuctionIdOrderByPriceDesc(auctionId)).thenReturn(bidList);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        when(productImageListResp.toProductImageUrlList()).thenReturn(productImageUrls);
        when(productImageService.getByProduct(product)).thenReturn(productImageListResp);

        AuctionDetailResponse actualResp = auctionService.getByAuctionId(auctionId);

        assertThat(actualResp.highestBidPrice()).isEqualTo(bidList.getFirst());
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
    void throwsWhenTryToGetIfAuctionNotExist() throws Exception {

        Long auctionId = 1L;
        List<BigDecimal> bidList = List.of(BigDecimal.valueOf(2000000L),
                BigDecimal.valueOf(1500000L), BigDecimal.valueOf(1000000L));

        when(bidRepository.findPricesByAuctionIdOrderByPriceDesc(auctionId)).thenReturn(bidList);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            auctionService.getByAuctionId(auctionId);
        }).isInstanceOf(GlobalException.class).hasMessage("해당 경매를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("auctionId로 경매를 삭제한다")
    void deleteByAuctionIdShouldSoftDeleteAuctionByAuctionId() throws Exception {

        Long auctionId = 1L;

        Auction auction = genAuctionCreateRequest().toEntity(genProduct());

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        auctionService.deleteByAuctionId(auctionId);

        verify(productService).deleteProduct(auction.getProduct());

        assertThat(auction.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 auctionId로 삭제를 시도하면 404 예외가 발생한다")
    void throwsWhenTryToDeleteIfAuctionNotExist() throws Exception {

        Long auctionId = 1L;

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            auctionService.deleteByAuctionId(auctionId);
        }).isInstanceOf(GlobalException.class).hasMessage("해당 경매를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("전체 경매 목록을 조회한다")
    void findAllShouldReturnAuctionList() throws Exception {

        List<Auction> mockAuctions = genAuctionList();
        when(auctionRepository.findAll()).thenReturn(mockAuctions);

        when(bidRepository.findPricesByAuctionIdOrderByPriceDesc(1L)).thenReturn(
                List.of(new BigDecimal("200000"), new BigDecimal("150000")));
        when(bidRepository.findPricesByAuctionIdOrderByPriceDesc(2L)).thenReturn(
                List.of(new BigDecimal("10000000")));
        when(bidRepository.findPricesByAuctionIdOrderByPriceDesc(3L)).thenReturn(
                List.of());


        List<AuctionListResponse> resp = auctionService.findAll();

        assertThat(resp).hasSize(3);

        // 1번 경매: maxPrice 200,000, bidCount 2
        assertThat(resp.get(0).maxPrice()).isEqualTo(new BigDecimal("200000"));
        assertThat(resp.get(0).bidCount()).isEqualTo(2);

        // 2번 경매: maxPrice 10,000,000, bidCount 1
        assertThat(resp.get(1).maxPrice()).isEqualTo(new BigDecimal("10000000"));
        assertThat(resp.get(1).bidCount()).isEqualTo(1);

        // 3번 경매: 입찰없음 -> maxPrice 0, bidCount 0
        assertThat(resp.get(2).maxPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(resp.get(2).bidCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("경매 목록이 없을 경우 빈 리스트를 반환한다")
    void findAllShouldReturnEmptyListWhenNoAuctionsExist() {

        when(auctionRepository.findAll()).thenReturn(Collections.emptyList());

        List<AuctionListResponse> resp = auctionService.findAll();

        assertThat(resp).isNotNull();
        assertThat(resp).isEmpty();
    }

}