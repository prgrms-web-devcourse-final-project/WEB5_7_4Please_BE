package com.deal4u.fourplease.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.dto.OrderUpdateRequest;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.order.repository.TempAuctionRepository;
import com.deal4u.fourplease.domain.order.repository.TempBidRepository;
import com.deal4u.fourplease.domain.order.repository.TempMemberRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private TempAuctionRepository tempAuctionRepository;
    @Mock
    private TempMemberRepository tempMemberRepository;
    @Mock
    private TempBidRepository tempBidRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Auction auction;
    private Member member;
    private OrderCreateRequest orderCreateRequest;
    private Address address;
    private Product product;
    private Orderer orderer;
    private OrderUpdateRequest orderUpdateRequest;

    @BeforeEach
    void setUp() {
        auction = Auction.builder()
                .auctionId(1L)
                .startingPrice(100L)
                .instantBidPrice(20000L)
                .deleted(false)
                .build();

        member = Member.builder()
                .memberId(1L)
                .email("pbk2312@inu.ac.kr")
                .nickName("당근을 흔들어라")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        orderCreateRequest = OrderCreateRequest.builder()
                .price(15000L)
                .memberId(1L)
                .build();

        address = new Address(
                "아크로 서울 포레스트 살고싶어요",
                "101동 202호",
                "12345"
        );


        product = Product.builder()
                .productId(1L)
                .name("맥북 프로")
                .thumbnailUrl("http://example.com/image.jpg")
                .seller(Seller.createSeller(member))
                .address(address)
                .build();

        orderer = Orderer.createOrderer(member);

        orderUpdateRequest = new OrderUpdateRequest(
                "한남 더힐",
                "101동",
                "54321",
                "010-9876-5432",
                "새로운 요청사항",
                "박유한"
        );
    }

    @Nested
    class CreateOrderTests {

        @Test
        @DisplayName("즉시 입찰가인 경우 주문이 정상적으로 생성되는 경우")
        void testCreateOrder_Successful() {
            // Given
            Long auctionId = 1L;
            String orderType = "BUY_NOW";
            Long memberId = 1L;

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                    auctionId)).thenReturn(
                    Optional.of(auction));
            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

            // When
            String orderId = orderService.saveOrder(auctionId, orderType, orderCreateRequest);

            // Then
            assertNotNull(orderId);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("낙찰자가 AWARD 타입의 주문을 정상적으로 생성하는 경우")
        void testCreateOrder_AwardSuccessful() {
            // Given
            String orderType = "AWARD";

            Bidder bidder = Bidder.createBidder(member);
            Bid winningBid = Bid.builder()
                    .bidId(1L)
                    .auction(auction)
                    .bidder(bidder)
                    .price(new BigDecimal("15000"))
                    .bidTime(LocalDateTime.now())
                    .isSuccessFulBidder(true)
                    .deleted(false)
                    .build();

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                    auction.getAuctionId()))
                    .thenReturn(Optional.of(auction));
            when(tempMemberRepository.findById(member.getMemberId()))
                    .thenReturn(Optional.of(member));
            when(tempBidRepository.existsSuccessfulBidder(auction.getAuctionId(), member))
                    .thenReturn(true);

            // When
            String orderId =
                    orderService.saveOrder(auction.getAuctionId(), orderType, orderCreateRequest);

            // Then
            assertNotNull(orderId);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(1)).save(orderCaptor.capture());

            Order capturedOrder = orderCaptor.getValue();

            assertThat(capturedOrder.getPrice()).isNotNull();
            assertThat(capturedOrder.getPrice()).isEqualByComparingTo(winningBid.getPrice());

            assertThat(capturedOrder.getAuction().getAuctionId()).isEqualTo(auction.getAuctionId());

            assertThat(capturedOrder.getPrice()).isGreaterThan(BigDecimal.ZERO);
        }


        @Test
        @DisplayName("유효하지 않은 주문 유형인 경우 예외 발생")
        void testCreateOrder_InvalidOrderType() {
            // Given
            Long auctionId = 1L;
            String orderType = "INVALID_TYPE";

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, orderCreateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("유효하지 않은 주문 타입입니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }


        @Test
        @DisplayName("경매를 찾을 수 없는 경우 예외 발생")
        void testCreateOrder_AuctionNotFound() {
            // Given
            Long auctionId = 1L;
            String orderType = "BUY_NOW";
            Long memberId = 1L;

            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                    auctionId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, orderCreateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 경매를 찾을 수 없습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("사용자를 찾을 수 없는 경우 예외 발생")
        void testCreateOrder_UserNotFound() {
            // Given
            Long auctionId = 1L;
            String orderType = "BUY_NOW";
            Long memberId = 1L;

            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, orderCreateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 유저를 찾을 수 없습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("AWARD 타입의 주문에서 유효하지 않은 입찰자일 경우 예외 발생")
        void testCreateOrder_InvalidBidderForAward() {
            // Given
            Long auctionId = 1L;
            String orderType = "AWARD";
            Long memberId = 1L;

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(
                    auctionId)).thenReturn(
                    Optional.of(auction));
            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(tempBidRepository.existsSuccessfulBidder(auctionId, member))
                    .thenReturn(false);

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, orderCreateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 사용자는 경매의 낙찰자가 아닙니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class GetOrderTests {

        @Test
        @DisplayName("주문 조회가 정상적으로 수행되는 경우")
        void testGetOrder_Successful() {
            // Given
            Long orderId = 1L;

            Auction auctionWithProduct = Auction.builder()
                    .auctionId(1L)
                    .product(product)
                    .build();

            Order orderWithProduct = Order.builder()
                    .id(orderId)
                    .orderId(OrderId.generate())
                    .price(new BigDecimal("100.0"))
                    .auction(auctionWithProduct)
                    .address(address)
                    .orderer(orderer)
                    .build();

            when(orderRepository.findByIdWithAuctionAndProduct(orderId)).thenReturn(
                    Optional.of(orderWithProduct));

            // When
            OrderResponse result = orderService.getOrder(orderId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.price()).isEqualTo(orderWithProduct.getPrice().longValue());
            assertThat(result.productName()).isEqualTo(
                    orderWithProduct.getAuction().getProduct().getName());
            assertThat(result.imageUrl()).isEqualTo(
                    orderWithProduct.getAuction().getProduct().getThumbnailUrl());

            verify(orderRepository, times(1)).findByIdWithAuctionAndProduct(orderId);
        }

        @Test
        @DisplayName("존재하지 않는 주문을 조회하는 경우 예외 발생")
        void testGetOrder_OrderNotFound() {
            // Given
            Long orderId = 999L;

            when(orderRepository.findByIdWithAuctionAndProduct(orderId)).thenReturn(
                    Optional.empty());

            // When, Then
            assertThatThrownBy(() -> orderService.getOrder(orderId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 주문을 찾을 수 없습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class UpdateOrderTests {

        @Test
        @DisplayName("주문 업데이트가 정상적으로 수행되는 경우")
        void testUpdateOrder_Successful() {
            // Given
            Long orderId = 1L;

            Address originalAddress = new Address(
                    "초가집",
                    "거지에요",
                    "12345"
            );

            Order orderToUpdate = Order.builder()
                    .id(orderId)
                    .orderId(OrderId.generate())
                    .price(new BigDecimal("100.0"))
                    .auction(auction)
                    .address(originalAddress)
                    .phone(null)
                    .content(null)
                    .receiver(null)
                    .orderer(orderer)
                    .build();

            when(orderRepository.findByIdWithAuctionAndProduct(orderId)).thenReturn(
                    Optional.of(orderToUpdate));

            // When
            orderService.updateOrder(orderId, orderUpdateRequest);

            // Then
            assertThat(orderToUpdate.getAddress().address()).isEqualTo("한남 더힐");
            assertThat(orderToUpdate.getAddress().detailAddress()).isEqualTo("101동");
            assertThat(orderToUpdate.getAddress().zipCode()).isEqualTo("54321");
            assertThat(orderToUpdate.getPhone()).isEqualTo("010-9876-5432");
            assertThat(orderToUpdate.getContent()).isEqualTo("새로운 요청사항");
            assertThat(orderToUpdate.getReceiver()).isEqualTo("박유한");

            verify(orderRepository, times(1)).findByIdWithAuctionAndProduct(orderId);
        }

        @Test
        @DisplayName("존재하지 않는 주문을 업데이트하려는 경우 예외 발생")
        void testUpdateOrder_OrderNotFound() {
            // Given
            Long orderId = 999L;

            when(orderRepository.findByIdWithAuctionAndProduct(orderId)).thenReturn(
                    Optional.empty());

            // When, Then
            assertThatThrownBy(() -> orderService.updateOrder(orderId, orderUpdateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 주문을 찾을 수 없습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);

            verify(orderRepository, times(1)).findByIdWithAuctionAndProduct(orderId);
        }
    }
}
