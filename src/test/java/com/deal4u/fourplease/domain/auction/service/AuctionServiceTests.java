package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.testutil.TestUtils.genAuctionCreateRequest;
import static com.deal4u.fourplease.testutil.TestUtils.genAuctionList;
import static com.deal4u.fourplease.testutil.TestUtils.genMember;
import static com.deal4u.fourplease.testutil.TestUtils.genMemberById;
import static com.deal4u.fourplease.testutil.TestUtils.genProduct;
import static com.deal4u.fourplease.testutil.TestUtils.genProductList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionSearchRequest;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.dto.CategoryDto;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.dto.ProductImageListResponse;
import com.deal4u.fourplease.domain.auction.dto.SellerInfoResponse;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.scheduler.AuctionScheduleService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTests {

    @InjectMocks
    private AuctionService auctionService;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private AuctionSupportService auctionSupportService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private BidService bidService;

    @Mock
    private AuctionScheduleService auctionScheduleService;

    @Mock
    private AuctionStatusService auctionStatusService;

    @Test
    @DisplayName("경매를 등록할 수 있다")
    void saveShouldSaveAuction() {

        Member member = genMember();
        AuctionCreateRequest req = genAuctionCreateRequest();

        Product product = genProduct();
        ProductCreateDto productDto = req.toProductCreateDto(member);

        Auction auctionWithoutId = req.toEntity(product);
        Auction savedAuctionWithId = Auction.builder()
                .auctionId(1L)
                .product(product)
                .duration(auctionWithoutId.getDuration())
                .build();

        LocalDateTime startDate = LocalDateTime.now();

        when(productService.save(productDto)).thenReturn(product);
        when(auctionRepository.save(any(Auction.class))).thenReturn(savedAuctionWithId);

        auctionService.save(req, member);

        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);

        verify(auctionRepository).save(auctionCaptor.capture());
        Auction auction = auctionCaptor.getValue();

        int bidPeriod = 3;

        assertThat(auction.getStartingPrice()).isEqualTo(req.startingPrice());
        assertThat(auction.getInstantBidPrice()).isEqualTo(req.buyNowPrice());
        assertThat(auction.getDuration().getStartTime()).isCloseTo(startDate,
                within(5, ChronoUnit.SECONDS));
        assertThat(auction.getDuration().getEndTime()).isCloseTo(
                startDate.plusDays(bidPeriod), within(5, ChronoUnit.SECONDS));
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.OPEN);
    }

    @Test
    @DisplayName("auctionId로 특정 경매를 조회 후 AuctionDetailResponse를 반환한다")
    void getByAuctionIdShouldReturnAuctionDetailResponse() {

        Long auctionId = 1L;

        BidSummaryDto bidSummaryDto = new BidSummaryDto(
                BigDecimal.valueOf(2000000L),
                3
        );

        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);

        ProductImageListResponse productImageListResponse = mock(ProductImageListResponse.class);
        List<String> productImageUrls = List.of("http://example.com/image1.jpg",
                "http://example.com/image2.jpg");

        when(bidService.getBidSummaryDto(auctionId)).thenReturn(bidSummaryDto);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));
        when(productImageService.getByProduct(product)).thenReturn(productImageListResponse);
        when(productImageListResponse.toProductImageUrls()).thenReturn(productImageUrls);

        AuctionDetailResponse actualResp = auctionService.getByAuctionId(auctionId, null);

        assertThat(actualResp.highestBidPrice()).isEqualTo(bidSummaryDto.maxPrice());
        assertThat(actualResp.instantBidPrice()).isEqualTo(auction.getInstantBidPrice());
        assertThat(actualResp.bidCount()).isEqualTo(bidSummaryDto.bidCount());
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
    void throwsWhenTryToGetIfAuctionNotExist() {
        Long auctionId = 1L;

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            auctionService.getByAuctionId(auctionId, null);
        }).isInstanceOf(GlobalException.class).hasMessage("해당 경매를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("auctionId로 경매를 삭제한다")
    void deleteByAuctionIdShouldSoftDeleteAuctionByAuctionId() {

        Long auctionId = 1L;

        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);
        Seller seller = product.getSeller();

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        auctionService.deleteByAuctionId(auctionId, seller.getMember());

        verify(productService).deleteProduct(auction.getProduct());

        assertThat(auction.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 auctionId로 삭제를 시도하면 404 예외가 발생한다")
    void throwsWhenTryToDeleteIfAuctionNotExist() {

        Long auctionId = 1L;

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            auctionService.deleteByAuctionId(auctionId, genMember());
        }).isInstanceOf(GlobalException.class).hasMessage("해당 경매를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("seller가 아닌 멤버가 경매 삭제를 시도하면 403 예외가 발생한다")
    void throwsWhenTryToDeleteByDifferentMemberFromSeller() {

        Long auctionId = 1L;

        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));


        assertThatThrownBy(() -> {
            auctionService.deleteByAuctionId(auctionId, genMemberById(2L));
        }).isInstanceOf(GlobalException.class).hasMessage("권한이 없습니다.");

    }

    @Test
    @DisplayName("판매자 id로 해당 판매자의 판매내역을 조회한다")
    void findSalesBySellerIdShouldReturnSellerSaleList() {

        Long sellerId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        List<Product> productList = genProductList();
        List<Auction> auctionList = genAuctionList();

        Page<Auction> auctionPage = new PageImpl<>(auctionList, pageable, auctionList.size());

        when(auctionRepository.findAllBySellerId(sellerId, pageable)).thenReturn(auctionPage);

        List<SellerSaleListResponse> responseList = List.of(
                SellerSaleListResponse.toSellerSaleListResponse(auctionList.get(0),
                        new BidSummaryDto(BigDecimal.valueOf(2000000), 5)),
                SellerSaleListResponse.toSellerSaleListResponse(auctionList.get(1),
                        new BidSummaryDto(BigDecimal.valueOf(10000000), 20)),
                SellerSaleListResponse.toSellerSaleListResponse(auctionList.get(2),
                        new BidSummaryDto(BigDecimal.valueOf(2000000), 20))
        );
        when(auctionSupportService.getSellerSaleListResponses(auctionPage))
                .thenReturn(new PageImpl<>(responseList, pageable, responseList.size()));

        PageResponse<SellerSaleListResponse> resp =
                auctionService.findSalesBySellerId(sellerId, pageable);

        assertThat(resp).isNotNull();
        assertThat(resp.getContent()).hasSize(3);
        assertThat(resp.getContent().get(0).name()).isEqualTo(productList.get(0).getName());
        assertThat(resp.getContent().get(0).thumbnailUrl())
                .isEqualTo(productList.get(0).getThumbnailUrl());
        assertThat(resp.getContent().get(1).name()).isEqualTo(productList.get(1).getName());
        assertThat(resp.getContent().get(1).thumbnailUrl())
                .isEqualTo(productList.get(1).getThumbnailUrl());
        assertThat(resp.getContent().get(2).name()).isEqualTo(productList.get(2).getName());
        assertThat(resp.getContent().get(2).thumbnailUrl())
                .isEqualTo(productList.get(2).getThumbnailUrl());

        assertThat(resp.getTotalElements()).isEqualTo(3);
        assertThat(resp.getTotalPages()).isEqualTo(1);
        assertThat(resp.getPage()).isZero();
        assertThat(resp.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("전체 경매 목록에서 검색어로 필터랑하고 최신순으로 정렬한다")
    void findAllShouldFillerByKeywordAndOrderByLatest() {

        Member member = genMember();

        AuctionSearchRequest req = new AuctionSearchRequest(
                0,
                20,
                "축구공",
                null,
                "latest"
        );

        Pageable pageable =
                PageRequest.of(req.page(), req.size(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Auction> auctionList = genAuctionList();

        // PageImpl로 Page 객체 모킹
        Page<Auction> auctionPage = new PageImpl<>(auctionList, pageable, auctionList.size());

        List<AuctionListResponse> auctionListResponseList = List.of(
                new AuctionListResponse(
                        2L,
                        "http://example.com/thumbnail2.jpg",
                        new CategoryDto(2L, "스포츠"),
                        "축구공",
                        BigDecimal.valueOf(10000000),
                        null,
                        150,
                        LocalDateTime.now().plusDays(7),
                        true
                )
        );
        Page<AuctionListResponse> auctionListResponsePage = new PageImpl<>(
                auctionListResponseList,
                pageable,
                auctionListResponseList.size()
        );

        when(auctionRepository.findByKeyword(req.keyword(), pageable)).thenReturn(auctionPage);
        when(auctionSupportService.getAuctionListResponses(auctionPage, member))
                .thenReturn(auctionListResponsePage);

        PageResponse<AuctionListResponse> resp =
                auctionService.findAll(req, member);

        assertThat(resp.getContent().getFirst().name()).isEqualTo("축구공");
        assertThat(resp.getTotalElements()).isEqualTo(auctionListResponseList.size());

    }

    @Test
    @DisplayName("전체 경매 목록에서 카테고리로 필터링하고 입찰순으로 필터링한다")
    void findAllShouldFilterByCategoryAndOrderByBidCount() {

        Member member = genMember();

        AuctionSearchRequest req = new AuctionSearchRequest(
                0,
                20,
                "",
                4L, // 생활용품
                "bids"
        );

        Pageable pageable = PageRequest.of(req.page(), req.size());
        List<Auction> auctionList = genAuctionList();

        Page<Auction> auctionPage = new PageImpl<>(auctionList, pageable, auctionList.size());

        List<AuctionListResponse> auctionListResponseList = List.of(
                new AuctionListResponse(
                        4L,
                        "http://example.com/thumbnail3.jpg",
                        new CategoryDto(4L, "생활용품"),
                        "치약",
                        BigDecimal.valueOf(2000000),
                        null,
                        40,
                        LocalDateTime.now(),
                        false
                ),
                new AuctionListResponse(
                        3L,
                        "http://example.com/thumbnail3.jpg",
                        new CategoryDto(4L, "생활용품"),
                        "칫솔",
                        BigDecimal.valueOf(2000000),
                        null,
                        20,
                        LocalDateTime.now(),
                        false
                )
        );

        Page<AuctionListResponse> auctionListResponsePage =
                new PageImpl<>(auctionListResponseList, pageable, auctionListResponseList.size());

        when(auctionRepository.findByCategoryIdOrderByBidCount(req.categoryId(), pageable))
                .thenReturn(auctionPage);
        when(auctionSupportService.getAuctionListResponses(auctionPage, member))
                .thenReturn(auctionListResponsePage);

        PageResponse<AuctionListResponse> resp = auctionService.findAll(req, member);

        assertThat(resp.getContent().getFirst().bidCount()).isEqualTo(40);
        assertThat(resp.getContent().getLast().bidCount()).isEqualTo(20);
        assertThat(resp.getTotalElements()).isEqualTo(auctionListResponseList.size());

    }

    @Test
    @DisplayName("auctionId로 판매자 정보를 조회한다")
    void getSellerInfoShouldReturnSellerInfoResponse() {
        // Given
        Long auctionId = 1L;
        Long sellerId = 100L;
        String sellerNickname = "박유한";
        LocalDateTime createdAt = LocalDateTime.of(2000, 5, 25, 10, 0, 0);

        Member member = Member.builder()
                .memberId(sellerId)
                .nickName(sellerNickname)
                .email("test@example.com")
                .provider("google")
                .build();

        ReflectionTestUtils.setField(member, "createdAt", createdAt);

        Seller seller = Seller.create(member);

        Product product = Product.builder()
                .seller(seller)
                .build();

        Auction auction = Auction.builder()
                .auctionId(auctionId)
                .product(product)
                .build();

        Integer totalReviews = 25;
        Double averageRating = 4.7;
        Integer completedDeals = 18;

        when(auctionRepository.findByIdWithProductAndSellerAndMember(auctionId)).thenReturn(
                Optional.of(auction));
        when(reviewRepository.countBySellerMemberId(sellerId)).thenReturn(totalReviews);
        when(reviewRepository.getAverageRatingBySellerMemberId(sellerId)).thenReturn(
                averageRating);
        when(auctionRepository.countBySellerIdAndStatus(sellerId, AuctionStatus.CLOSE))
                .thenReturn(completedDeals);

        // When
        SellerInfoResponse response = auctionService.getSellerInfo(auctionId);

        // Then
        assertThat(response.sellerId()).isEqualTo(sellerId);
        assertThat(response.sellerNickname()).isEqualTo(sellerNickname);
        assertThat(response.totalReviews()).isEqualTo(totalReviews);
        assertThat(response.averageRating()).isEqualTo(4.7);
        assertThat(response.completedDeals()).isEqualTo(completedDeals);
        assertThat(response.createdAt()).isEqualTo(createdAt.toString());
    }

    @Test
    @DisplayName("리뷰가 없는 판매자의 평균 평점은 0.0으로 반환된다")
    void getSellerInfoShouldReturnZeroRatingWhenNoReviews() {
        // Given
        Long auctionId = 1L;
        Long sellerId = 100L;
        String sellerNickname = "박유한";
        LocalDateTime createdAt = LocalDateTime.of(2000, 5, 25, 10, 0, 0);

        Member member = Member.builder()
                .memberId(sellerId)
                .nickName(sellerNickname)
                .email("test@example.com")
                .provider("google")
                .build();

        ReflectionTestUtils.setField(member, "createdAt", createdAt);

        Seller seller = Seller.create(member);

        Product product = Product.builder()
                .seller(seller)
                .build();

        Auction auction = Auction.builder()
                .auctionId(auctionId)
                .product(product)
                .build();

        Integer totalReviews = 0;
        Double averageRating = null; // 리뷰가 없을 때 null 반환
        Integer completedDeals = 2;

        when(auctionRepository.findByIdWithProductAndSellerAndMember(auctionId)).thenReturn(
                Optional.of(auction));
        when(reviewRepository.countBySellerMemberId(sellerId)).thenReturn(totalReviews);
        when(reviewRepository.getAverageRatingBySellerMemberId(sellerId)).thenReturn(averageRating);
        when(auctionRepository.countBySellerIdAndStatus(sellerId, AuctionStatus.CLOSE))
                .thenReturn(completedDeals);

        // When
        SellerInfoResponse response = auctionService.getSellerInfo(auctionId);

        // Then
        assertThat(response.sellerId()).isEqualTo(sellerId);
        assertThat(response.sellerNickname()).isEqualTo(sellerNickname);
        assertThat(response.totalReviews()).isEqualTo(totalReviews);
        assertThat(response.averageRating()).isEqualTo(0.0);
        assertThat(response.completedDeals()).isEqualTo(completedDeals);
        assertThat(response.createdAt()).isEqualTo(createdAt.toString());
    }


    @Test
    @DisplayName("평균 평점이 소수점 둘째 자리까지 반올림되어 반환된다")
    void getSellerInfoShouldRoundAverageRatingToTwoDecimalPlaces() {
        // Given
        Long auctionId = 1L;
        Long sellerId = 100L;
        String sellerNickname = "박유한";
        LocalDateTime createdAt = LocalDateTime.of(2000, 5, 25, 10, 0, 0);

        Member member = Member.builder()
                .memberId(sellerId)
                .nickName(sellerNickname)
                .email("test@example.com")
                .provider("google")
                .build();

        ReflectionTestUtils.setField(member, "createdAt", createdAt);

        Seller seller = Seller.create(member);

        Product product = Product.builder()
                .seller(seller)
                .build();

        Auction auction = Auction.builder()
                .auctionId(auctionId)
                .product(product)
                .build();

        Integer totalReviews = 127;
        Double averageRating = 4.6789; // 소수점 넷째 자리까지 있는 평점
        Integer completedDeals = 89;

        when(auctionRepository.findByIdWithProductAndSellerAndMember(auctionId)).thenReturn(
                Optional.of(auction));
        when(reviewRepository.countBySellerMemberId(sellerId)).thenReturn(totalReviews);
        when(reviewRepository.getAverageRatingBySellerMemberId(sellerId)).thenReturn(averageRating);
        when(auctionRepository.countBySellerIdAndStatus(sellerId, AuctionStatus.CLOSE))
                .thenReturn(completedDeals);

        // When
        SellerInfoResponse response = auctionService.getSellerInfo(auctionId);

        // Then
        assertThat(response.sellerId()).isEqualTo(sellerId);
        assertThat(response.sellerNickname()).isEqualTo(sellerNickname);
        assertThat(response.totalReviews()).isEqualTo(totalReviews);
        assertThat(response.averageRating()).isEqualTo(4.68); // 소수점 둘째 자리까지 반올림
        assertThat(response.completedDeals()).isEqualTo(completedDeals);
        assertThat(response.createdAt()).isEqualTo(createdAt.toString());
    }

    @Test
    @DisplayName("존재하지 않는 auctionId로 판매자 정보 조회를 시도하면 404 예외가 발생한다")
    void getSellerInfoShouldThrowExceptionWhenAuctionNotFound() {
        // Given
        Long auctionId = 999L;

        when(auctionRepository.findByIdWithProductAndSellerAndMember(auctionId)).thenReturn(
                Optional.empty());

        // When & Then
        assertThatThrownBy(() -> auctionService.getSellerInfo(auctionId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("해당 경매를 찾을 수 없습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("로그인한 사용자가 찜한 경매 상품 상세 조회 시 isWishList는 true를 반환한다")
    void getByAuctionIdWithLoggedInMemberShouldReturnIsWishListTrue() {
        // Given
        Long auctionId = 1L;
        Member member = genMember(); // 로그인한 사용자
        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);

        Wishlist wishlist = Wishlist.builder()
                .wishlistId(10L)
                .memberId(member.getMemberId())
                .auction(auction)
                .build();

        BidSummaryDto bidSummaryDto = new BidSummaryDto(
                BigDecimal.valueOf(2000000L),
                3
        );
        List<String> productImageUrls =
                List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg");

        ProductImageListResponse productImageListResponse = mock(ProductImageListResponse.class);

        when(bidService.getBidSummaryDto(auctionId)).thenReturn(bidSummaryDto);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        when(productImageService.getByProduct(product)).thenReturn(productImageListResponse);
        when(productImageListResponse.toProductImageUrls()).thenReturn(productImageUrls);

        when(wishlistRepository.findWishlist(auction, member.getMemberId())).thenReturn(
                Optional.of(wishlist));

        // When
        AuctionDetailResponse actualResp = auctionService.getByAuctionId(auctionId, member);

        // Then
        assertThat(actualResp.isWishList()).isTrue();
        assertThat(actualResp.productName()).isEqualTo(product.getName());
        assertThat(actualResp.highestBidPrice()).isEqualTo(bidSummaryDto.maxPrice());
        assertThat(actualResp.imageUrls()).isEqualTo(productImageUrls);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 경매 상품 상세 조회 시 isWishList는 false를 반환한다")
    void getByAuctionIdWithNoMemberShouldReturnIsWishListFalse() {
        // Given
        Long auctionId = 1L;
        Product product = genProduct();
        Auction auction = genAuctionCreateRequest().toEntity(product);

        BidSummaryDto bidSummaryDto = new BidSummaryDto(
                BigDecimal.valueOf(2000000L),
                3
        );
        List<String> productImageUrls =
                List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg");
        ProductImageListResponse productImageListResponse = mock(ProductImageListResponse.class);

        // Mocking 설정
        when(bidService.getBidSummaryDto(auctionId)).thenReturn(bidSummaryDto);
        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));
        when(productImageService.getByProduct(product)).thenReturn(productImageListResponse);
        when(productImageListResponse.toProductImageUrls()).thenReturn(productImageUrls);

        // When
        // member 파라미터에 null을 전달하여 비로그인 상태를 시뮬레이션
        AuctionDetailResponse actualResp = auctionService.getByAuctionId(auctionId, null);

        // Then
        assertThat(actualResp.isWishList()).isFalse(); // isWishList가 false인지 확인
        assertThat(actualResp.productName()).isEqualTo(product.getName());
    }
}
