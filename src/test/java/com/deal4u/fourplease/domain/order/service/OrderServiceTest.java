package com.deal4u.fourplease.domain.order.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.order.repository.TempAuctionRepository;
import com.deal4u.fourplease.domain.order.repository.TempMemberRepository;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private SettlementRepository settlementRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Auction auction;
    private Member member;
    private OrderCreateRequest orderCreateRequest;

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
                .build();

        orderCreateRequest = OrderCreateRequest.builder()
                .memberId(1L)
                .build();
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

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalse(auctionId)).thenReturn(
                    Optional.of(auction));
            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

            // When
            String orderId = orderService.createOrder(auctionId, orderType, orderCreateRequest);

            // Then
            assertNotNull(orderId);
            verify(orderRepository, times(1)).save(any(Order.class));
        }


        @Test
        @DisplayName("낙찰자가 AWARD 타입의 주문을 정상적으로 생성하는 경우")
        void testCreateOrder_AwardSuccessful() {
            // Given
            Long auctionId = 1L;
            String orderType = "AWARD";
            Long memberId = 1L;


            when(tempAuctionRepository.findByAuctionIdAndDeletedFalse(auctionId)).thenReturn(
                    Optional.of(auction));
            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

            when(settlementRepository.existsByAuctionAndBidderAndStatus(eq(auction),
                    any(Bidder.class), eq(SettlementStatus.PENDING)))
                    .thenReturn(true);

            // When
            String orderId = orderService.createOrder(auctionId, orderType, orderCreateRequest);

            // Then
            assertNotNull(orderId);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("유효하지 않은 주문 유형인 경우 예외 발생")
        void testCreateOrder_InvalidOrderType() {
            // Given
            Long auctionId = 1L;
            String orderType = "INVALID_TYPE";

            // When, Then
            assertThatThrownBy(
                    () -> orderService.createOrder(auctionId, orderType, orderCreateRequest))
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

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalse(auctionId)).thenReturn(
                    Optional.empty());

            // When, Then
            assertThatThrownBy(
                    () -> orderService.createOrder(auctionId, orderType, orderCreateRequest))
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

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalse(auctionId)).thenReturn(
                    Optional.of(auction));
            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(
                    () -> orderService.createOrder(auctionId, orderType, orderCreateRequest))
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

            when(tempAuctionRepository.findByAuctionIdAndDeletedFalse(auctionId)).thenReturn(
                    Optional.of(auction));
            when(tempMemberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(settlementRepository.existsByAuctionAndBidderAndStatus(eq(auction), any(),
                    eq(SettlementStatus.PENDING))).thenReturn(false);

            // When, Then
            assertThatThrownBy(
                    () -> orderService.createOrder(auctionId, orderType, orderCreateRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 사용자는 경매의 낙찰자가 아닙니다.")
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
