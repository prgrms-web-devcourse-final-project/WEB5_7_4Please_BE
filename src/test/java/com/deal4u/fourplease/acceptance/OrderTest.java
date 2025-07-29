package com.deal4u.fourplease.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.dto.OrderUpdateRequest;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.response.ExtractableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("주문 관련 인수 테스트")
@Transactional
class OrderTest extends MockMvcBaseAcceptTest {

    @Test
    @DisplayName("BUY_NOW 타입으로 주문 생성 성공")
    void createOrderBuyNowSuccessful() {
        // given
        Long auctionId = 1L; // 열린 경매
        String orderType = "BUY_NOW";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(800000L) // 즉시 구매가
                .build();

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.body().asString()).isNotBlank(); // 주문 ID 반환
    }

    @Test
    @DisplayName("AWARD 타입으로 주문 생성 성공")
    void createOrderAwardSuccessful() {
        // given
        Long auctionId = 3L; // 닫힌 경매
        String orderType = "AWARD";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(2000L) // 낙찰가
                .build();

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(3L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.body().asString()).isNotBlank(); // 주문 ID 반환
    }

    @Test
    @DisplayName("잘못된 주문 타입으로 주문 생성 실패")
    void createOrderInvalidOrderType() {
        // given
        Long auctionId = 1L;
        String orderType = "INVALID_TYPE";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(50000L)
                .build();

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("잘못된 가격으로 BUY_NOW 주문 생성 실패")
    void createOrderBuyNowInvalidPrice() {
        // given
        Long auctionId = 1L;
        String orderType = "BUY_NOW";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(10000L) // 즉시 구매가와 다른 가격
                .build();

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("주문 조회 성공")
    void getOrderSuccessful() {
        // given
        // 먼저 주문 생성
        Long auctionId = 1L;
        String orderType = "BUY_NOW";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(800000L)
                .build();

        String orderId = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .extract()
                .body()
                .asString();

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .when()
                .get("/api/v1/orders/{orderId}", orderId)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        OrderResponse orderResponse = response.body().as(OrderResponse.class);
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.price()).isEqualTo(800000L);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 실패")
    void getOrderNotFound() {
        // given
        Long nonExistentOrderId = 400L;

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .when()
                .get("/api/v1/orders/{orderId}", nonExistentOrderId)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("주문 업데이트 성공")
    void updateOrderSuccessful() {
        // given
        // 먼저 주문 생성
        Long auctionId = 1L;
        String orderType = "BUY_NOW";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(800000L)
                .build();

        String orderId = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .extract()
                .body()
                .asString();

        // 업데이트 요청 준비
        OrderUpdateRequest orderUpdateRequest = new OrderUpdateRequest(
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "010-1234-5678",
                "문 앞에 놓아주세요",
                "홍길동"
        );

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderUpdateRequest)
                .when()
                .put("/api/v1/orders/{orderId}", orderId)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        // 업데이트된 주문 조회하여 확인
        ExtractableResponse<MockMvcResponse> getResponse = authRequest(21L)
                .when()
                .get("/api/v1/orders/{orderId}", orderId)
                .then()
                .log().all().assertThat().statusCode(HttpStatus.OK.value())
                .extract();

        OrderResponse updatedOrder = getResponse.body().as(OrderResponse.class);
        assertThat(updatedOrder.address()).isEqualTo("서울시 강남구");
        assertThat(updatedOrder.addressDetail()).isEqualTo("테헤란로 123");
        assertThat(updatedOrder.zipCode()).isEqualTo("12345");
        assertThat(updatedOrder.phone()).isEqualTo("010-1234-5678");
        assertThat(updatedOrder.deliveryRequest()).isEqualTo("문 앞에 놓아주세요");
        assertThat(updatedOrder.recipient()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 주문 업데이트 실패")
    void updateOrderNotFound() {
        // given
        String nonExistentOrderId = "non-existent-order-id";
        OrderUpdateRequest orderUpdateRequest = new OrderUpdateRequest(
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "010-1234-5678",
                "문 앞에 놓아주세요",
                "홍길동"
        );

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderUpdateRequest)
                .when()
                .put("/api/v1/orders/{orderId}", nonExistentOrderId)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("유효하지 않은 주문 업데이트 요청 실패")
    void updateOrderInvalidRequest() {
        // given
        // 먼저 주문 생성
        Long auctionId = 1L;
        String orderType = "BUY_NOW";
        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .price(50000L)
                .build();

        String orderId = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(orderCreateRequest)
                .when()
                .post("/api/v1/auctions/{auctionId}/orders/{type}", auctionId, orderType)
                .then()
                .extract()
                .body()
                .asString();

        // 유효하지 않은 업데이트 요청 (전화번호 형식 오류)
        OrderUpdateRequest invalidOrderUpdateRequest = new OrderUpdateRequest(
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "invalid-phone-number", // 잘못된 전화번호 형식
                "문 앞에 놓아주세요",
                "홍길동"
        );

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidOrderUpdateRequest)
                .when()
                .put("/api/v1/orders/{orderId}", orderId)
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
