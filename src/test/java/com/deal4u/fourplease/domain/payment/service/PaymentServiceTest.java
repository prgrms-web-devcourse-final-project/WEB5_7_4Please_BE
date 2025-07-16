package com.deal4u.fourplease.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private Payment payment;

    @InjectMocks
    private PaymentService paymentService;

    private TossPaymentConfirmRequest confirmRequest;
    private Order buyNowOrder;
    private Order awardOrder;
    private Auction auction;
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
                100000
        );

        buyNowOrder = Order.builder()
                .orderId(OrderId.create("order123"))
                .auction(auction)
                .price(new BigDecimal("100000"))
                .orderType(OrderType.BUY_NOW)
                .build();

        awardOrder = Order.builder()
                .orderId(OrderId.create("order123"))
                .auction(auction)
                .price(new BigDecimal("80000"))
                .orderType(OrderType.AWARD)
                .build();

        successResponse = new TossPaymentConfirmResponse(
                "order123",
                "paymentKey123",
                "DONE",
                "2025-07-12",
                "2025-07-12",
                100000
        );

        failedResponse = new TossPaymentConfirmResponse(
                "order123",
                "paymentKey123",
                "FAILED",
                "2025-07-12",
                "2025-07-12",
                100000
        );
    }

    @Nested
    class PaymentConfirmTest {

        @Test
        @DisplayName("결제가 성공적으로 되고 상태가 변경되었다.")
        void paymentConfirmSuccess() {
            // given
            given(paymentTransactionService.getOrderOrThrow(any())).willReturn(buyNowOrder);
            given(namedLockProvider.getBottleLock(anyString())).willReturn(namedLock);
            given(bidRepository.findMaxBidPriceByAuctionId(1L)).willReturn(
                    Optional.of(new BigDecimal("90000")));
            given(tossApiClient.confirmPayment(confirmRequest)).willReturn(successResponse);
            given(paymentTransactionService.savePayment(any(), any(), any(), any())).willReturn(
                    payment);

            // when
            paymentService.paymentConfirm(confirmRequest);

            // then
            verify(paymentTransactionService).paymentStatusSuccess(payment);
            verify(orderService).succesOrder(buyNowOrder);
            verify(orderService).closeAuction(auction);
            verify(namedLock).unlock();
        }

        @Test
        @DisplayName("결제승인실패시_결제상태가_실패로_변경된다")
        void priceMismatch() {
            // given
            given(paymentTransactionService.getOrderOrThrow(any())).willReturn(buyNowOrder);
            given(namedLockProvider.getBottleLock(anyString())).willReturn(namedLock);
            given(bidRepository.findMaxBidPriceByAuctionId(1L)).willReturn(
                    Optional.of(new BigDecimal("90000")));
            given(tossApiClient.confirmPayment(confirmRequest)).willReturn(failedResponse);
            given(paymentTransactionService.savePayment(any(), any(), any(), any())).willReturn(
                    payment);

            // when & then
            assertThatThrownBy(() -> paymentService.paymentConfirm(confirmRequest))
                    .isInstanceOf(GlobalException.class);

            verify(paymentTransactionService).updatePaymentStatusToFailed(payment, buyNowOrder);
            verify(orderService).succesOrder(buyNowOrder);
            verify(orderService).closeAuction(auction);
        }

        @Test
        @DisplayName("결제 금액불일치시 주문이 실패로 변경된다")
        void priceUnmatch() {
            // given
            TossPaymentConfirmRequest invalidAmountRequest = new TossPaymentConfirmRequest(
                    "paymentKey123",
                    "order123",
                    50000 // 주문 금액과 다른 금액
            );
            given(paymentTransactionService.getOrderOrThrow(any())).willReturn(buyNowOrder);

            // when & then
            assertThatThrownBy(() -> paymentService.paymentConfirm(invalidAmountRequest))
                    .isInstanceOf(GlobalException.class);

            verify(orderService).faliedOrder(buyNowOrder);
        }

        @Test
        @DisplayName("즉시구매가격이 현재최고입찰가보다 낮으면 주문이 실패로_변경된다")
        void buyNowPriceTooLow() {
            // given
            given(paymentTransactionService.getOrderOrThrow(any())).willReturn(buyNowOrder);
            given(namedLockProvider.getBottleLock(anyString())).willReturn(namedLock);
            given(bidRepository.findMaxBidPriceByAuctionId(1L)).willReturn(
                    Optional.of(new BigDecimal("150000"))); // 즉시구매가보다 높은 입찰가

            // when & then
            assertThatThrownBy(() -> paymentService.paymentConfirm(confirmRequest))
                    .isInstanceOf(GlobalException.class);

            verify(orderService).faliedOrder(buyNowOrder);
        }
    }

    @Test
    @DisplayName("결제 실패 시 상태가 FAILED로 변경된다")
    void paymentFailureStatus() {
        // given
        TossPaymentConfirmResponse failedResponse = new TossPaymentConfirmResponse(
                "order123",
                "paymentKey123",
                "FAILED",
                "2025-07-12",
                "2025-07-12",
                100000
        );
        given(paymentTransactionService.getOrderOrThrow(any())).willReturn(buyNowOrder);
        given(namedLockProvider.getBottleLock(anyString())).willReturn(namedLock);
        given(tossApiClient.confirmPayment(confirmRequest)).willReturn(failedResponse);
        given(paymentTransactionService.savePayment(any(), any(), any(), any())).willReturn(
                payment);

        // when & then
        assertThatThrownBy(() -> paymentService.paymentConfirm(confirmRequest))
                .isInstanceOf(GlobalException.class);

        verify(paymentTransactionService).updatePaymentStatusToFailed(payment, buyNowOrder);
    }
}
