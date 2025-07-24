package com.deal4u.fourplease.acceptance;

import static com.deal4u.fourplease.domain.auction.entity.BidPeriod.THREE;
import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.auction.service.ProductService;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("경매관련 인수 테스트")
class AuctionTest extends BaseAcceptTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("경매가 생성되고 조회 된다")
    @Transactional
    void createAuction() {
        AuctionCreateRequest auctionCreateRequest = new AuctionCreateRequest(
                "김 조던",
                "김조던은 어쩌구 저쩌구 입니다",
                "http://deal4U.com/thumbnail.jpg",
                List.of("http://deal4U.com/image.jpg"),
                1L,
                "서울시 관악구 신림동",
                "몰라~~",
                "624803",
                "010-1111-1111",
                LocalDateTime.now(),
                THREE,
                BigDecimal.valueOf(10000L),
                null
        );
        authRequest(1L)
                .body(auctionCreateRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.CREATED.value());

        List<AuctionListResponse> responses = request()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/api/v1/auctions?keyword={keyword}", auctionCreateRequest.productName())
                .then().assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().category().categoryId()).isEqualTo(
                auctionCreateRequest.categoryId());
        assertThat(responses.getFirst().name()).isEqualTo(auctionCreateRequest.productName());
        assertThat(responses.getFirst().thumbnailUrl()).isEqualTo(
                auctionCreateRequest.thumbnailUrl());
    }


    @Test
    @DisplayName("경매를 생성하고 상세 조회 한다")
    @Transactional
    void selectDetail() {

        List<AuctionListResponse> responses = request()
                .get("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        AuctionListResponse first = responses.getFirst();

        AuctionDetailResponse auctionDetailResponse = request()
                .get("/api/v1/auctions/{id}/description", first.auctionId())
                .then().assertThat().statusCode(HttpStatus.OK.value())
                .extract().body().as(AuctionDetailResponse.class);

        Auction auction = auctionRepository.findById(first.auctionId())
                .orElseThrow(AssertionError::new);
        assertThat(auctionDetailResponse.description()).isEqualTo(
                auction.getProduct().getDescription());
        assertThat(auctionDetailResponse.categoryId()).isEqualTo(
                auction.getProduct().getCategory().getCategoryId());
        assertThat(auctionDetailResponse.thumbnailUrl()).isEqualTo(
                auction.getProduct().getThumbnailUrl());
        assertThat(auctionDetailResponse.startingPrice()).isEqualTo(auction.getStartingPrice());
        assertThat(auctionDetailResponse.productName()).isEqualTo(auction.getProduct().getName());
    }
}
