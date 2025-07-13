package com.deal4u.fourplease.domain.payment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.payment.config.TossApiClient;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import java.math.BigDecimal;
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

    @InjectMocks
    private PaymentService paymentService;

    private TossPaymentConfirmRequest confirmRequest;
    private Order order;
    private TossPaymentConfirmResponse successResponse;
    private TossPaymentConfirmResponse failedResponse;

    @BeforeEach
    void setUp() {
        confirmRequest = new TossPaymentConfirmRequest(
                "paymentKey123",
                "order123",
                10000
        );

        order = Order.builder()
                .orderId(OrderId.create("order123"))
                .price(new BigDecimal("10000"))
                .build();

        successResponse = new TossPaymentConfirmResponse(
                "order123",
                "paymentKey123",
                "DONE",
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
    @DisplayName("결제 승인 성공 - 정상적인 결제 처리")
    void paymentConfirm_Success_Improved() {
        // given
        when(paymentTransactionService.getOrderOrThrow(any(OrderId.class)))
                .thenReturn(order);
        when(namedLockProvider.getBottleLock(anyString()))
                .thenReturn(namedLock);
        when(tossApiClient.confirmPayment(confirmRequest))
                .thenReturn(successResponse);

        doNothing().when(namedLock).lock();
        doNothing().when(namedLock).unlock();

        // when
        paymentService.paymentConfirm(confirmRequest);

        // then
        verify(paymentTransactionService).getOrderOrThrow(any(OrderId.class));
        verify(namedLock).lock();
        verify(tossApiClient).confirmPayment(confirmRequest);
        verify(paymentTransactionService).savePayment(order, confirmRequest, successResponse);
        verify(namedLock).unlock();
        verify(namedLockProvider).getBottleLock(order.getOrderId().toString());
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
        when(namedLockProvider.getBottleLock(order.getOrderId().toString()))
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
}
