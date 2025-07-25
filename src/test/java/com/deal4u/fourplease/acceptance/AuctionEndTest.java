package com.deal4u.fourplease.acceptance;

import static com.deal4u.fourplease.domain.auction.entity.BidPeriod.THREE;
import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.response.ExtractableResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("경매 마감 관련 인수 테스트")
@Transactional
class AuctionEndTest extends MockMvcBaseAcceptTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Test
    @DisplayName("경매 마감 시간이 지난 후 입찰 시도 시 실패")
    void bidAfterAuctionEndFails() {
        // given - 이미 종료된 경매 생성
        LocalDateTime pastStartTime = LocalDateTime.now().minusDays(4);
        AuctionCreateRequest auctionCreateRequest = new AuctionCreateRequest(
                "테스트 상품",
                "테스트 상품 설명입니다",
                "http://deal4U.com/thumbnail.jpg",
                List.of("http://deal4U.com/image.jpg"),
                1L,
                "서울시 관악구 신림동",
                "상세 주소",
                "12345",
                "010-1234-5678",
                pastStartTime,
                THREE, // 3일 기간 (이미 종료됨)
                BigDecimal.valueOf(10000L),
                null
        );

        // 경매 생성
        authRequest(1L)
                .body(auctionCreateRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // 생성된 경매 조회
        List<Auction> auctions = auctionRepository.findAll();
        Auction createdAuction = auctions.stream()
                .filter(auction -> auction.getProduct().getName().equals("테스트 상품"))
                .findFirst()
                .orElseThrow();

        // 경매 상태를 CLOSED로 변경
        createdAuction.close();
        auctionRepository.save(createdAuction);

        // when - 종료된 경매에 입찰 시도
        BidRequest bidRequest = new BidRequest(createdAuction.getAuctionId(), 15000);
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then - 입찰 실패 (경매가 종료됨)
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("경매 상태가 CLOSED로 변경되면 입찰 불가")
    void bidOnClosedAuctionFails() {
        // given - 진행 중인 경매 생성
        LocalDateTime currentStartTime = LocalDateTime.now();
        AuctionCreateRequest auctionCreateRequest = new AuctionCreateRequest(
                "진행 중 상품",
                "진행 중인 상품 설명입니다",
                "http://deal4U.com/thumbnail.jpg",
                List.of("http://deal4U.com/image.jpg"),
                1L,
                "서울시 관악구 신림동",
                "상세 주소",
                "12345",
                "010-1234-5678",
                currentStartTime,
                THREE,
                BigDecimal.valueOf(10000L),
                null
        );

        // 경매 생성
        authRequest(1L)
                .body(auctionCreateRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // 생성된 경매 조회
        List<Auction> auctions = auctionRepository.findAll();
        Auction createdAuction = auctions.stream()
                .filter(auction -> auction.getProduct().getName().equals("진행 중 상품"))
                .findFirst()
                .orElseThrow();

        // 경매 상태를 CLOSED로 변경
        createdAuction.close();
        auctionRepository.save(createdAuction);

        // when - 종료된 경매에 입찰 시도
        BidRequest bidRequest = new BidRequest(createdAuction.getAuctionId(), 15000);
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then - 입찰 실패 (경매가 종료됨)
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("경매 상태가 FAIL로 변경되면 입찰 불가")
    void bidOnFailedAuctionFails() {
        // given - 진행 중인 경매 생성
        LocalDateTime currentStartTime = LocalDateTime.now();
        AuctionCreateRequest auctionCreateRequest = new AuctionCreateRequest(
                "실패 예정 상품",
                "실패 예정 상품 설명입니다",
                "http://deal4U.com/thumbnail.jpg",
                List.of("http://deal4U.com/image.jpg"),
                1L,
                "서울시 관악구 신림동",
                "상세 주소",
                "12345",
                "010-1234-5678",
                currentStartTime,
                THREE,
                BigDecimal.valueOf(10000L),
                null
        );

        // 경매 생성
        authRequest(1L)
                .body(auctionCreateRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // 생성된 경매 조회
        List<Auction> auctions = auctionRepository.findAll();
        Auction createdAuction = auctions.stream()
                .filter(auction -> auction.getProduct().getName().equals("실패 예정 상품"))
                .findFirst()
                .orElseThrow();

        // 경매 상태를 FAIL로 변경
        createdAuction.fail();
        auctionRepository.save(createdAuction);

        // when - 실패한 경매에 입찰 시도
        BidRequest bidRequest = new BidRequest(createdAuction.getAuctionId(), 15000);
        ExtractableResponse<MockMvcResponse> response = authRequest(21L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(bidRequest)
                .when()
                .post("/api/v1/bids")
                .then()
                .extract();

        // then - 입찰 실패 (경매가 실패함)
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}