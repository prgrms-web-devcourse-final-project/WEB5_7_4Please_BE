package com.deal4u.fourplease.domain.payment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.OrderType;
import com.deal4u.fourplease.domain.order.service.OrderService;
import com.deal4u.fourplease.domain.payment.config.TossApiClient;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TossApiClient tossApiClient;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private NamedLockProvider namedLockProvider;

    @Mock
    private NamedLock namedLock;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private TossPaymentConfirmRequest confirmRequest;
    private Order order;
    private Order buyNowOrder;
    private Auction auction;
    private Payment payment;
    private TossPaymentConfirmResponse successResponse;
    private TossPaymentConfirmResponse failedResponse;

    @BeforeEach
    void setUp() {
        auction = Auction.builder()
                .auctionId(1L)
                .product(null)
                .startingPrice(new BigDecimal("50000"))
                .instantBidPrice(new BigDecimal("100000"))
                .duration(null)
                .status(AuctionStatus.OPEN)
                .deleted(false)
                .build();

        confirmRequest = new TossPaymentConfirmRequest(
                "paymentKey123",
                "order123",
                10000
        );

        order = Order.builder()
                .orderId(OrderId.create("order123"))
                .auction(auction)
                .price(new BigDecimal("10000"))
                .orderType(OrderType.AWARD)
                .build();

        buyNowOrder = Order.builder()
                .orderId(OrderId.create("order123"))
                .auction(auction)
                .price(new BigDecimal("100000"))
                .orderType(OrderType.BUY_NOW)
                .build();

        payment = Payment.builder()
                .paymentId(1L)
                .paymentKey("paymentKey123")
                .amount(new BigDecimal("10000"))
                .build();

        successResponse = new TossPaymentConfirmResponse(
                "DONE",
                "paymentKey123",
                "order123",
                "2025-07-12",
                "2025-07-12",
                10000
        );

        failedResponse = new TossPaymentConfirmResponse(
                "FAILED",
                "paymentKey123",
                "order123",
                "2025-07-12",
                "2025-07-12",
                10000
        );
    }

    @Test
    @DisplayName("일반 경매 결제 승인 성공")
    void paymentConfirm_Success_NormalBid() {
        // given
        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(order);
        when(namedLockProvider.getBottleLock(anyString()))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(confirmRequest))
                .thenReturn(successResponse);
        when(paymentTransactionService.savePayment(any(), any(), any(), any()))
                .thenReturn(payment);

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when
        assertDoesNotThrow(() -> paymentService.paymentConfirm(confirmRequest));

        // then
        verify(paymentTransactionService).getOrderOrThrow(any(OrderId.class));
        verify(namedLock).lock();
        verify(tossApiClient).confirmPayment(confirmRequest);
        verify(paymentTransactionService).savePayment(order, confirmRequest, successResponse,
                auction);
        verify(orderService).closeAuction(auction);
        verify(namedLock).unlock();
    }

    @Test
    @DisplayName("즉시 구매 결제 승인 성공")
    void paymentConfirm_Success_BuyNow() {
        // given
        TossPaymentConfirmRequest buyNowRequest = new TossPaymentConfirmRequest(
                "paymentKey123",
                "order123",
                100000
        );

        TossPaymentConfirmResponse buyNowResponse = new TossPaymentConfirmResponse(
                "DONE",
                "paymentKey123",
                "order123",
                "2025-07-12",
                "2025-07-12",
                100000
        );

        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(buyNowOrder);
        when(namedLockProvider.getBottleLock(anyString()))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(buyNowRequest))
                .thenReturn(buyNowResponse);
        when(paymentTransactionService.savePayment(any(), any(), any(), any()))
                .thenReturn(payment);
        when(bidRepository.findMaxBidPriceByAuctionId(1L))
                .thenReturn(Optional.of(new BigDecimal("80000")));

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when
        assertDoesNotThrow(() -> paymentService.paymentConfirm(buyNowRequest));

        // then
        verify(bidRepository).findMaxBidPriceByAuctionId(1L);
        verify(orderService).closeAuction(auction);
        verify(paymentTransactionService, never()).updatePaymentStatusToFailed(any(), any());
    }

    @Test
    @DisplayName("즉시 구매 결제 실패 - 현재 최고 입찰가가 즉시 구매 가격보다 높음")
    void paymentConfirm_BuyNow_Failed_InvalidPrice() {
        // given
        TossPaymentConfirmRequest buyNowRequest = new TossPaymentConfirmRequest(
                "paymentKey123",
                "order123",
                100000
        );

        TossPaymentConfirmResponse buyNowResponse = new TossPaymentConfirmResponse(
                "DONE",
                "paymentKey123",
                "order123",
                "2025-07-12",
                "2025-07-12",
                100000
        );

        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(buyNowOrder);
        when(namedLockProvider.getBottleLock(anyString()))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(buyNowRequest))
                .thenReturn(buyNowResponse);
        when(paymentTransactionService.savePayment(any(), any(), any(), any()))
                .thenReturn(payment);
        when(bidRepository.findMaxBidPriceByAuctionId(1L))
                .thenReturn(Optional.of(new BigDecimal("100000"))); // 같은 가격

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when & then
        assertThatThrownBy(() -> paymentService.paymentConfirm(buyNowRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage("즉시 구매 가격이 현재 최고 입찰가보다 높아야 합니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(paymentTransactionService).updatePaymentStatusToFailed(payment, buyNowOrder);
        verify(orderService, never()).closeAuction(auction);
    }

    @Test
    @DisplayName("결제 승인 실패 - 주문을 찾을 수 없음")
    void paymentConfirm_OrderNotFound() {
        // given
        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenThrow(ErrorCode.ORDER_NOT_FOUND.toException());

        // when & then
        assertThatThrownBy(() -> paymentService.paymentConfirm(confirmRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage("해당 주문을 찾을 수 없습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("결제 승인 실패 - 토스 API 응답 상태가 실패")
    void paymentConfirm_PaymentStatusFailed() {
        // given
        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(order);
        when(namedLockProvider.getBottleLock(anyString()))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(confirmRequest))
                .thenReturn(failedResponse);

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when & then
        assertThatThrownBy(() -> paymentService.paymentConfirm(confirmRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage("결제 승인이 실패했습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(namedLock).unlock();
    }

    @Test
    @DisplayName("결제 승인 실패 - 결제 금액이 주문 금액과 다름")
    void paymentConfirm_InvalidAmount() {
        // given
        TossPaymentConfirmRequest invalidAmountRequest = new TossPaymentConfirmRequest(
                "paymentKey123",
                "order123",
                20000  // 금액이 잘못된 경우
        );

        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(order);

        // when & then
        assertThatThrownBy(() -> paymentService.paymentConfirm(invalidAmountRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage("결제 금액이 주문 금액과 일치하지 않습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("결제 승인 실패 - 토스 API 호출 오류")
    void paymentConfirm_TossApiError() {
        // given
        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(order);
        when(namedLockProvider.getBottleLock(anyString()))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(confirmRequest))
                .thenThrow(FeignException.class);

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when & then
        assertThatThrownBy(() -> paymentService.paymentConfirm(confirmRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage("결제 처리 중 오류가 발생했습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(namedLock).unlock();
    }

    @Test
    @DisplayName("결제 처리 중 예외 발생 시 결제 실패로 업데이트 및 락 해제")
    void paymentConfirm_ExceptionHandling() {
        // given
        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(order);
        when(namedLockProvider.getBottleLock(anyString()))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(confirmRequest))
                .thenReturn(successResponse);
        when(paymentTransactionService.savePayment(any(), any(), any(), any()))
                .thenReturn(payment)
                .thenThrow(ErrorCode.PAYMENT_CONFIRMATION_FAILED.toException());

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when & then
        assertThatThrownBy(() -> paymentService.paymentConfirm(confirmRequest))
                .isInstanceOf(GlobalException.class);

        verify(paymentTransactionService).updatePaymentStatusToFailed(payment, order);
        verify(namedLock).unlock();
    }

    @Test
    @DisplayName("락 사용 검증 - 정확한 키로 락 획득")
    void paymentConfirm_LockKeyVerification() {
        // given
        String expectedLockKey = "auction-lock:" + auction.getAuctionId();

        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(order);
        when(namedLockProvider.getBottleLock(expectedLockKey))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(confirmRequest))
                .thenReturn(successResponse);
        when(paymentTransactionService.savePayment(any(), any(), any(), any()))
                .thenReturn(payment);

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when
        assertDoesNotThrow(() -> paymentService.paymentConfirm(confirmRequest));

        // then
        verify(namedLockProvider).getBottleLock(expectedLockKey);
        verify(namedLock, times(1)).lock();
        verify(namedLock, times(1)).unlock();
    }
}
