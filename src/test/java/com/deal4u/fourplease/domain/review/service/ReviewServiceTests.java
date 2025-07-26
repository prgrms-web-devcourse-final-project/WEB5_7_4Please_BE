package com.deal4u.fourplease.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.OrderStatus;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import com.deal4u.fourplease.domain.payment.entity.PaymentStatus;
import com.deal4u.fourplease.domain.payment.repository.PaymentRepository;
import com.deal4u.fourplease.domain.review.dto.ReviewRequest;
import com.deal4u.fourplease.domain.review.dto.ReviewResponse;
import com.deal4u.fourplease.domain.review.entity.Review;
import com.deal4u.fourplease.domain.review.entity.Reviewer;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTests {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private Member seller;
    private Member buyer;
    private Product product;
    private Auction auction;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        this.seller = Member.builder()
                .memberId(1L)
                .email("seller1@gmail.com")
                .nickName("판매자1")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        this.buyer = Member.builder()
                .memberId(2L)
                .email("buyer@example.com")
                .nickName("구매자1")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        this.product = Product.builder()
                .productId(1L)
                .name("맥북 프로")
                .thumbnailUrl("http://example.com/image.jpg")
                .seller(Seller.create(seller))
                .build();

        this.auction = Auction.builder()
                .auctionId(1L)
                .product(product)
                .startingPrice(new BigDecimal("100"))
                .instantBidPrice(new BigDecimal("20000"))
                .status(AuctionStatus.CLOSE)
                .deleted(false)
                .build();

        this.order = Order.builder()
                .orderId(OrderId.generate())
                .auction(auction)
                .orderer(Orderer.createOrderer(buyer))
                .status(OrderStatus.SUCCESS)
                .price(new BigDecimal("300000"))
                .build();

        this.payment = Payment.builder()
                .paymentId(1L)
                .amount(order.getPrice())
                .status(PaymentStatus.SUCCESS)
                .orderId(order.getOrderId())
                .build();


    }

    @Test
    @DisplayName("리뷰 작성 성공")
    void create_review_success() {

        // Given
        Long buyerId = buyer.getMemberId();

        // Mocking repository
        when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                auction.getAuctionId())).thenReturn(Optional.of(auction));
        when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrdererAndAuctionAndStatus(any(Orderer.class),
                any(Auction.class), any(OrderStatus.class))).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(payment));
        when(reviewRepository.findByAuctionAndReviewer(any(Auction.class),
                any(Reviewer.class))).thenReturn(Optional.empty());

        ReviewRequest request = new ReviewRequest(
                auction.getAuctionId(),
                5,
                "사장님이 맛있고, 음식이 친절해요.");
      
        // When
        reviewService.createReview(request, buyerId);

        // Then
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();

        assertThat(savedReview.getRating()).isEqualTo(request.rating());
        assertThat(savedReview.getContent()).isEqualTo(request.content());
        assertThat(savedReview.getAuction()).isEqualTo(auction);
        assertThat(savedReview.getReviewer().getReviewer().getMemberId()).isEqualTo(buyerId);
        assertThat(savedReview.getSeller().getMember().getMemberId()).isEqualTo(
                seller.getMemberId());

    }

    @Test
    @DisplayName("리뷰 작성 실패 (존재하지 않는 경매)")
    void create_review_not_found_auction() {
        // Given
        Long nonExistingAuctionId = 99L;
        ReviewRequest request = new ReviewRequest(nonExistingAuctionId, 5, "...");
        Long buyerId = buyer.getMemberId();

        // When
        when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                nonExistingAuctionId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> reviewService.createReview(request, buyerId))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.AUCTION_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("리뷰 작성 실패 (존재하지 않는 주문 내역)")
    void create_review_not_found_order() {
        // Given
        ReviewRequest request = new ReviewRequest(auction.getAuctionId(), 5, "...");
        Long buyerId = buyer.getMemberId();

        when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                auction.getAuctionId())).thenReturn(Optional.of(auction));
        when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrdererAndAuctionAndStatus(any(Orderer.class),
                any(Auction.class), any(OrderStatus.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(request, buyerId))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("리뷰 작성 실패 (존재하지 않는 결제 내역)")
    void create_review_not_found_payment() {
        // Given
        ReviewRequest request = new ReviewRequest(auction.getAuctionId(), 5, "...");
        Long buyerId = buyer.getMemberId();

        when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                auction.getAuctionId())).thenReturn(Optional.of(auction));
        when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrdererAndAuctionAndStatus(any(Orderer.class),
                any(Auction.class), any(OrderStatus.class))).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(request, buyerId))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.PAYMENT_NOT_SUCCESS.getMessage());
    }

    @Test
    @DisplayName("리뷰 작성 실패 (이미 작성된 리뷰가 존재하는 경우)")
    void create_review_already_exists_review() {
        // Given
        ReviewRequest request = new ReviewRequest(auction.getAuctionId(), 5, "...");
        Long buyerId = buyer.getMemberId();
        Review existingReview = Review.builder()
                .build();

        when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                auction.getAuctionId())).thenReturn(Optional.of(auction));
        when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrdererAndAuctionAndStatus(any(Orderer.class),
                any(Auction.class), any(OrderStatus.class))).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(payment));
        when(reviewRepository.findByAuctionAndReviewer(any(Auction.class),
                any(Reviewer.class))).thenReturn(Optional.of(existingReview));

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(request, buyerId))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.REVIEW_ALREADY_EXISTS.getMessage());

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("판매자 리뷰 목록 조회 성공")
    void get_review_list_for_member_success() {
        // Given
        Long sellerId = seller.getMemberId();
        Pageable pageable = PageRequest.of(0, 10);

        Review review1 = Review.builder()
                .reviewId(1L).content("좋은 상품입니다! 기존 리뷰").rating(4)
                .seller(Seller.create(seller)).reviewer(Reviewer.create(buyer))
                .build();
        ReflectionTestUtils.setField(review1, "createdAt", LocalDateTime.now().minusDays(1));

        Review review2 = Review.builder()
                .reviewId(2L).content("좋은 상품입니다! 최신 리뷰").rating(2)
                .seller(Seller.create(seller)).reviewer(Reviewer.create(buyer))
                .build();
        ReflectionTestUtils.setField(review2, "createdAt", LocalDateTime.now());

        List<Review> sortedReview = List.of(review2, review1);
        Page<Review> reviewPage = new PageImpl<>(sortedReview, pageable, sortedReview.size());

        when(memberRepository.findByMemberIdAndStatus(sellerId, Status.ACTIVE)).thenReturn(
                Optional.of(seller));
        when(reviewRepository.findBySeller(any(Seller.class), any(Pageable.class))).thenReturn(
                reviewPage);

        // When
        PageResponse<ReviewResponse> response = reviewService.getReviewListFor(sellerId,
                pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().getFirst().content()).isEqualTo("좋은 상품입니다! 최신 리뷰");
        assertThat(response.getContent().getLast().content()).isEqualTo("좋은 상품입니다! 기존 리뷰");

        verify(memberRepository).findByMemberIdAndStatus(sellerId, Status.ACTIVE);
        verify(reviewRepository).findBySeller(any(Seller.class), any(Pageable.class));
    }

    @Test
    @DisplayName("판매자 리뷰 목록 조회 실패 (존재하지 않거나 비활성 상태인 회원)")
    void get_review_list_for_member_fail_member_not_found_or_not_active() {
        // Given
        Long nonExistentMemberId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(memberRepository.findByMemberIdAndStatus(nonExistentMemberId,
                Status.ACTIVE)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(
                () -> reviewService.getReviewListFor(nonExistentMemberId, pageable))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        // Then
        verify(reviewRepository, never()).findBySeller(any(Seller.class), any(Pageable.class));
    }
}