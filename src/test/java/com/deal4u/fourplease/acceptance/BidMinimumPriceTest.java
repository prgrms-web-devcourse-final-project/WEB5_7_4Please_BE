package com.deal4u.fourplease.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.response.ExtractableResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("입찰 최소 금액 관련 인수 테스트")
@Transactional
class BidMinimumPriceTest extends MockMvcBaseAcceptTest {

    @DisplayName("동일 사용자가 자신의 이전 입찰보다 낮은 금액으로 재입찰 시 실패")
    @Test
    void sameBidderWithLowerPriceThanPreviousBidFails() {
        // given - 첫 번째 입찰 (150,000원)
        BidRequest initialBidRequest = new BidRequest(1L, 150000);
        authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(initialBidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when - 동일 사용자가 더 낮은 금액으로 재입찰 시도 (100,000원)
        BidRequest lowerBidRequest = new BidRequest(1L, 100000);
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(lowerBidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then - 입찰 실패 (자신의 이전 입찰보다 낮음)
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("동일 사용자가 자신의 이전 입찰보다 높은 금액으로 재입찰 시 성공")
    void sameBidderWithHigherPriceThanPreviousBidSucceeds() {
        // given - 첫 번째 입찰 (100,000원)
        BidRequest initialBidRequest = new BidRequest(1L, 100000);
        authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(initialBidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when - 동일 사용자가 더 높은 금액으로 재입찰 (150,000원)
        BidRequest higherBidRequest = new BidRequest(1L, 150000);
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(higherBidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then - 입찰 성공
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // 입찰 목록 조회하여 최고 입찰가 확인
        ExtractableResponse<MockMvcResponse> getBidsResponse = request()
                .when()
                .get("/api/v1/auctions/1/bids?page=0&size=30")
                .then()
                .extract();

        // 최고 입찰가가 150,000원인지 확인
        List<BidResponse> content = getBidsResponse.jsonPath()
                .getList("content", BidResponse.class);
        BidResponse highestBid =  content.stream()
                .filter(bid -> bid.memberId() == 21L)
                .max((bid1, bid2) -> Integer.compare(bid1.bidPrice(), bid2.bidPrice()))
                .orElseThrow();


        assertThat(highestBid.bidPrice()).isEqualTo(150000);
    }
}