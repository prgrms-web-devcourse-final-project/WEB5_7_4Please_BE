package com.deal4u.fourplease.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
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
import com.deal4u.fourplease.domain.review.entity.Review;
import com.deal4u.fourplease.domain.review.entity.Reviewer;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .status(AuctionStatus.CLOSED)
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
    void create_review_success() throws Exception {
        // Given
        ReviewRequest request = new ReviewRequest(
                auction.getAuctionId(),
                5,
                "사장님이 맛있고, 음식이 친절해요.");
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
    void create_review_not_found_auction() throws Exception {
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
    void create_review_not_found_order() throws Exception {
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
    void create_review_not_found_payment() throws Exception {
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
    void create_review_already_exists_review() throws Exception {
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
}