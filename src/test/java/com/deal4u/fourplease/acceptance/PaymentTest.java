package com.deal4u.fourplease.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE_TIME;
import static org.mockito.ArgumentMatchers.any;

import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.payment.config.TossApiClient;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.repository.PaymentRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("결제 인수 테스트")
@Transactional
class PaymentTest extends MockMvcBaseAcceptTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private TossApiClient tossApiClient;

    @Test
    @DisplayName("정산 후 정상 결제 성공")
    void awardOrderpaymentConfirmSuccess() {
        // given
        String paymentKey = "test-payment-key";
        String orderId = "ORD-20250726-0002";
        int amount = 2000;// 즉시구매가와 일치
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, orderId,
                amount);

        Mockito.when(tossApiClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
                .thenReturn(new TossPaymentConfirmResponse(
                        orderId,paymentKey,"DONE",LOCAL_DATE_TIME.toString(),LOCAL_DATE_TIME.toString(),amount));

        // when
        var response = authRequest(3L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v1/payments/confirm")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("즉시 구매 요청 후 결제 진행 ")
    void buyNowOrderpaymentConfirmSuccess2() {
        // given
        Long auctionId = 6L;
        String orderType = "BUY_NOW";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(50000L)
                .build();

        String orderId = authRequest(3L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .log().all().assertThat().statusCode(HttpStatus.CREATED.value())
                .extract()
                .body()
                .asString();

        String paymentKey = "test-payment-key";
        int amount = 50000;// 즉시구매가와 일치
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, orderId, amount);

        Mockito.when(tossApiClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
                .thenReturn(new TossPaymentConfirmResponse(
                        orderId,paymentKey,"DONE",LOCAL_DATE_TIME.toString(),LOCAL_DATE_TIME.toString(),amount));

        // when
        var response = authRequest(3L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v1/payments/confirm")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 결제 시도시 404 반환")
    void paymentConfirmOrderNotFound() {
        // given
        String paymentKey = "test-payment-key";
        String orderId = "999-999"; // 존재하지 않는 주문
        int amount = 1000;
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, orderId,
                amount);

        // when
        var response = authRequest(3L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v1/payments/confirm")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
//
    @Test
    @DisplayName("금액 불일치로 결제 실패시 400 반환")
    void paymentConfirmAmountMismatch() {
        // given
        String paymentKey = "test-payment-key";
        String orderId = "ORD-20250726-0002"; // 존재하는 주문
        int amount = 1000; // 실제 주문 금액과 다름
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, orderId,
                amount);

        // when
        var response = authRequest(3L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v1/payments/confirm")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("외부 결제 API 실패시 400 반환")
    void paymentConfirmTossApiFail() {
        // given
        String paymentKey = "test-payment-key";
        String orderId = "ORD-20250726-0002";
        int amount = 2000;
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, orderId,
                amount);

        Mockito.when(tossApiClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
                .thenThrow(FeignException.class);

        // when
        var response = authRequest(3L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v1/payments/confirm")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
} 