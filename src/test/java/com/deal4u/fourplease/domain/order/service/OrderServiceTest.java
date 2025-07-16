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
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.dto.OrderUpdateRequest;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.OrderStatus;
import com.deal4u.fourplease.domain.order.entity.OrderType;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
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
    private AuctionRepository auctionRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Auction openAuction;
    private Auction closedAuction;
    private Member member;
    private OrderCreateRequest orderCreateRequest;
    private OrderCreateRequest awardOrderCreateRequest;
    private Address address;
    private Product product;
    private Orderer orderer;
    private OrderUpdateRequest orderUpdateRequest;
    private Bid winningBid;

    @BeforeEach
    void setUp() {
        openAuction = Auction.builder()
                .auctionId(1L)
                .startingPrice(new BigDecimal("100"))
                .instantBidPrice(new BigDecimal("20000"))
                .status(AuctionStatus.OPEN)
                .deleted(false)
                .build();

        closedAuction = Auction.builder()
                .auctionId(2L)
                .startingPrice(new BigDecimal("100"))
                .instantBidPrice(new BigDecimal("20000"))
                .status(AuctionStatus.CLOSED)
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
                .price(20000L)
                .build();

        awardOrderCreateRequest = OrderCreateRequest.builder()
                .price(15000L)
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
                .seller(Seller.create(member))
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

        Bidder bidder = Bidder.createBidder(member);
        winningBid = Bid.builder()
                .bidId(1L)
                .auction(closedAuction)
                .bidder(bidder)
                .price(new BigDecimal("15000"))
                .bidTime(LocalDateTime.now())
                .isSuccessfulBidder(true)
                .deleted(false)
                .build();
    }

    @Nested
    class CreateOrderTests {

        @Test
        @DisplayName("즉시 구매(BUY_NOW) 주문이 정상적으로 생성되는 경우")
        void testCreateOrder_BuyNowSuccessful() {
            // Given
            Long auctionId = 1L;
            String orderType = "BUY_NOW";

            when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusOpen(auctionId))
                    .thenReturn(Optional.of(openAuction));
            when(memberRepository.findById(1L))
                    .thenReturn(Optional.of(member));

            // When
            String orderId = orderService.saveOrder(auctionId, orderType, orderCreateRequest);

            // Then
            assertNotNull(orderId);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(1)).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getPrice()).isEqualByComparingTo(
                    openAuction.getInstantBidPrice());
            assertThat(savedOrder.getOrderType()).isEqualTo(OrderType.BUY_NOW);
            assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("즉시 구매(BUY_NOW) 주문에서 가격이 일치하지 않는 경우 예외 발생")
        void testCreateOrder_BuyNowInvalidPrice() {
            // Given
            Long auctionId = 1L;
            String orderType = "BUY_NOW";

            OrderCreateRequest invalidPriceRequest = OrderCreateRequest.builder()
                    .price(10000L) // 즉시 구매가(20000L)와 다른 가격
                    .build();

            when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusOpen(auctionId))
                    .thenReturn(Optional.of(openAuction));
            when(memberRepository.findById(1L))
                    .thenReturn(Optional.of(member));

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, invalidPriceRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("요청된 가격이 즉시 입찰가와 일치하지 않습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("낙찰자가 AWARD 타입의 주문을 정상적으로 생성하는 경우")
        void testCreateOrder_AwardSuccessful() {
            // Given
            Long auctionId = 2L;
            String orderType = "AWARD";

            when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(auctionId))
                    .thenReturn(Optional.of(closedAuction));
            when(memberRepository.findById(1L))
                    .thenReturn(Optional.of(member));
            when(bidRepository.findSuccessFulBid(auctionId, member))
                    .thenReturn(Optional.of(winningBid));

            // When
            String orderId = orderService.saveOrder(auctionId, orderType, awardOrderCreateRequest);

            // Then
            assertNotNull(orderId);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(1)).save(orderCaptor.capture());

            Order capturedOrder = orderCaptor.getValue();
            assertThat(capturedOrder.getPrice()).isEqualByComparingTo(winningBid.getPrice());
            assertThat(capturedOrder.getAuction().getAuctionId()).isEqualTo(auctionId);
            assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.AWARD);
            assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("AWARD 타입의 주문에서 가격이 일치하지 않는 경우 예외 발생")
        void testCreateOrder_AwardInvalidPrice() {
            // Given
            Long auctionId = 2L;
            String orderType = "AWARD";

            OrderCreateRequest invalidPriceRequest = OrderCreateRequest.builder()
                    .price(10000L)  // 낙찰가(15000L)와 다른 가격
                    .build();

            when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(auctionId))
                    .thenReturn(Optional.of(closedAuction));
            when(memberRepository.findById(1L))
                    .thenReturn(Optional.of(member));
            when(bidRepository.findSuccessFulBid(auctionId, member))
                    .thenReturn(Optional.of(winningBid));

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, invalidPriceRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("요청된 가격이 낙찰가와 일치하지 않습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("BUY_NOW 타입에서 OPEN 상태가 아닌 경매에 대한 주문 시 예외 발생")
        void testCreateOrder_BuyNowAuctionNotOpen() {
            // Given
            Long auctionId = 1L;
            String orderType = "BUY_NOW";

            when(memberRepository.findById(1L))
                    .thenReturn(Optional.of(member));
            when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusOpen(auctionId))
                    .thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, orderCreateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 경매를 찾을 수 없습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("AWARD 타입에서 CLOSED 상태가 아닌 경매에 대한 주문 시 예외 발생")
        void testCreateOrder_AwardAuctionNotClosed() {
            // Given
            Long auctionId = 2L;
            String orderType = "AWARD";

            when(memberRepository.findById(1L))
                    .thenReturn(Optional.of(member));
            when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(auctionId))
                    .thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, awardOrderCreateRequest))
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

            when(memberRepository.findById(1L))
                    .thenReturn(Optional.empty());

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
            Long auctionId = 2L;
            String orderType = "AWARD";

            when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(auctionId))
                    .thenReturn(Optional.of(closedAuction));
            when(memberRepository.findById(1L))
                    .thenReturn(Optional.of(member));
            when(bidRepository.findSuccessFulBid(auctionId, member))
                    .thenReturn(Optional.empty());  // 낙찰 입찰이 없음

            // When, Then
            assertThatThrownBy(
                    () -> orderService.saveOrder(auctionId, orderType, awardOrderCreateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 사용자는 경매의 낙찰자가 아닙니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("유효하지 않은 주문 타입인 경우 예외 발생")
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

            when(orderRepository.findByIdWithAuctionAndProduct(orderId))
                    .thenReturn(Optional.of(orderWithProduct));

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

            when(orderRepository.findByIdWithAuctionAndProduct(orderId))
                    .thenReturn(Optional.empty());

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
                    .auction(openAuction)
                    .address(originalAddress)
                    .phone(null)
                    .content(null)
                    .receiver(null)
                    .orderer(orderer)
                    .build();

            when(orderRepository.findByIdWithAuctionAndProduct(orderId))
                    .thenReturn(Optional.of(orderToUpdate));

            // When
            orderService.updateOrder(orderId, orderUpdateRequest);

            // Then
            assertThat(orderToUpdate.getAddress().address()).isEqualTo("한남 더힐");
            assertThat(orderToUpdate.getAddress().addressDetail()).isEqualTo("101동");
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

            when(orderRepository.findByIdWithAuctionAndProduct(orderId))
                    .thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(() -> orderService.updateOrder(orderId, orderUpdateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 주문을 찾을 수 없습니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);

            verify(orderRepository, times(1)).findByIdWithAuctionAndProduct(orderId);
        }
    }

    @Nested
    class CloseAuctionTests {

        @Test
        @DisplayName("경매 종료가 정상적으로 수행되는 경우")
        void testCloseAuction_Successful() {
            // Given
            Auction auctionToClose = Auction.builder()
                    .auctionId(1L)
                    .status(AuctionStatus.OPEN)
                    .build();

            // When
            orderService.closeAuction(auctionToClose);

            verify(auctionRepository, times(0)).save(any());
        }
    }
}
