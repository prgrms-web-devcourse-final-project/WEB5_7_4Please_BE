package com.deal4u.fourplease.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import com.deal4u.fourplease.domain.common.PageResponse;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.response.ExtractableResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("입찰관련 인수 테스트")
@Transactional
class BidTest extends MockMvcBaseAcceptTest {

    @Test
    @DisplayName("입찰 생성 성공")
    void createBid() {
        // given
        BidRequest bidRequest = new BidRequest(1L, 100000);

        // when
        ExtractableResponse<MockMvcResponse> response =
                // 맴버 21번의 정보를 취득하여서 `accessToken`을 생성
                authRequest(21L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        // `/api/v1/bids` (POST)의 `requestBody(bidRequest)`를 추가
                        .body(bidRequest)
                        .when()
                        .post("/api/v1/bids")
                        .then()
                        .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("입찰 생성 실패 - 인증되지 않은 사용자")
    void createBidWithoutAuth() {
        // given
        BidRequest bidRequest = new BidRequest(1L, 100000);

        // when
        ExtractableResponse<MockMvcResponse> response = request()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("입찰 생성 실패 - 잘못된 요청 데이터 (경매 ID 누락)")
    void createBidWithInvalidData() {
        // given
        BidRequest bidRequest = new BidRequest(null, 100000);

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("입찰 생성 실패 - 잘못된 가격 (0 이하)")
    void createBidWithInvalidPrice() {
        // given
        BidRequest bidRequest = new BidRequest(1L, -1000);

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("경매별 입찰 목록 조회 성공")
    void getBidsByAuction() {
        // given - 먼저 입찰을 생성
        BidRequest bidRequest1 = new BidRequest(4L, 100000);
        BidRequest bidRequest2 = new BidRequest(4L, 120000);

        authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest1)
                .when()
                .post("/api/v1/bids");

        authRequest(22L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest2)
                .when()
                .post("/api/v1/bids");

        // when
        ExtractableResponse<MockMvcResponse> response = request()
                .when()
                .get("/api/v1/auctions/4/bids?page=0&size=10")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        PageResponse<BidResponse> pageResponse = response.as(PageResponse.class);
        assertThat(pageResponse.getContent()).isNotEmpty();
        assertThat(pageResponse.getContent().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("경매별 입찰 목록 조회 - 존재하지 않는 경매")
    void getBidsByNonExistentAuction() {
        // when
        ExtractableResponse<MockMvcResponse> response = request()
                .when()
                .get("/api/v1/auctions/999/bids?page=0&size=10")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("입찰 삭제 성공")
    void deleteBid() {
        // Given
        // 생성된 입찰 목록을 조회하여 bidId를 얻음
        ExtractableResponse<MockMvcResponse> getBidsResponse = request()
                .when()
                .get("/api/v1/auctions/4/bids?page=0&size=10")
                .then()
                .extract();

        List<BidResponse> pageResponse = getBidsResponse.jsonPath()
                .getList("content", BidResponse.class);
        assertThat(pageResponse).isNotEmpty();

        // 첫 번째 입찰의 ID를 가져옴 (실제로는 memberId가 21L인 입찰을 찾아야 함)
        Long bidId = (pageResponse.getLast()).bidId();

        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .when()
                .delete("/api/v1/bids/" + bidId)
                .then()
                .extract();
        // then
        // 현재 `dev`에서는 `OK`가 아닌, `NO_CONTENT`로 되어 있기 때문에 테스트 코드 수정 시에 수정 예정.
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("입찰 삭제 실패 - 인증되지 않은 사용자")
    void deleteBidWithoutAuth() {
        // when
        ExtractableResponse<MockMvcResponse> response = request()
                .when()
                .delete("/api/v1/bids/1")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("입찰 삭제 실패 - 존재하지 않는 입찰")
    void deleteNonExistentBid() {
        // when
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .when()
                .delete("/api/v1/bids/999")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("입찰 업데이트 (동일 사용자가 더 높은 가격으로 재입찰)")
    void updateBidWithHigherPrice() {
        // given - 첫 번째 입찰
        BidRequest initialBidRequest = new BidRequest(1L, 100000);
        authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(initialBidRequest)
                .when()
                .post("/api/v1/bids");

        // when - 동일 사용자가 더 높은 가격으로 재입찰
        BidRequest updateBidRequest = new BidRequest(1L, 150000);
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(updateBidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("입찰 업데이트 실패 - 동일 사용자가 더 낮은 가격으로 재입찰")
    void updateBidWithLowerPrice() {
        // given - 첫 번째 입찰
        BidRequest initialBidRequest = new BidRequest(1L, 150000);
        authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(initialBidRequest)
                .when()
                .post("/api/v1/bids");

        // when - 동일 사용자가 더 낮은 가격으로 재입찰
        BidRequest updateBidRequest = new BidRequest(1L, 100000);
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(updateBidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("여러 사용자의 입찰 경쟁 시나리오")
    void multipleBiddersCompetition() {
        // given & when - 여러 사용자가 순차적으로 입찰
        BidRequest bid1 = new BidRequest(1L, 100000);
        BidRequest bid2 = new BidRequest(1L, 120000);
        BidRequest bid3 = new BidRequest(1L, 150000);

        // 사용자 21이 첫 번째 입찰
        ExtractableResponse<MockMvcResponse> response1 = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bid1)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // 사용자 22가 더 높은 가격으로 입찰
        ExtractableResponse<MockMvcResponse> response2 = authRequest(22L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bid2)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // 사용자 21이 다시 더 높은 가격으로 입찰
        ExtractableResponse<MockMvcResponse> response3 = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bid3)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then
        assertThat(response1.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response2.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response3.statusCode()).isEqualTo(HttpStatus.OK.value());

        // 입찰 목록 조회하여 검증
        ExtractableResponse<MockMvcResponse> getBidsResponse = request()
                .when()
                .get("/api/v1/auctions/1/bids?page=0&size=10")
                .then()
                .extract();

        assertThat(getBidsResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        PageResponse<BidResponse> pageResponse = getBidsResponse.as(PageResponse.class);
        assertThat(pageResponse.getContent()).isNotEmpty();
        assertThat(pageResponse.getContent().size()).isGreaterThanOrEqualTo(
                2); // 최소 2개의 입찰 (사용자별로 최신 입찰만 유지)
    }
}
