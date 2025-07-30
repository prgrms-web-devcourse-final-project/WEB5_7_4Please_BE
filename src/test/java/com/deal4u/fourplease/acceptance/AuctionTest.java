package com.deal4u.fourplease.acceptance;

import static com.deal4u.fourplease.domain.auction.entity.BidPeriod.THREE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionImageUrlResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.file.service.FileSaver;
import com.deal4u.fourplease.domain.file.service.SavePath;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("경매관련 인수 테스트")
@TestInstance(Lifecycle.PER_METHOD)
@Transactional
class AuctionTest extends MockMvcBaseAcceptTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @MockitoBean
    private FileSaver fileSaver;

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
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
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

    @Test
    @DisplayName("경매를 등록하고 경매를 취소 시킨다")
    @Transactional
    void deleteAuction() {
        long memberId = 1L;
        AuctionCreateRequest auctionCreateRequest = new AuctionCreateRequest(
                "김 조던",
                "김조던은 어쩌구 저쩌구 입니다",
                "http://deal4U.com/thumbnail.jpg",
                List.of("http://deal4U.com/image.jpg"),
                memberId,
                "서울시 관악구 신림동",
                "몰라~~",
                "624803",
                "010-1111-1111",
                THREE,
                BigDecimal.valueOf(10000L),
                null
        );
        authRequest(memberId)
                .body(auctionCreateRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.CREATED.value());

        List<AuctionListResponse> responses = request()
                .get("/api/v1/auctions?keyword={keyword}", auctionCreateRequest.productName())
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        authRequest(memberId)
                .body(auctionCreateRequest)
                .when()
                .delete("/api/v1/auctions/{id}", responses.getFirst().auctionId())
                .then().assertThat().statusCode(HttpStatus.NO_CONTENT.value());

    }

    @Test
    @DisplayName("경매를 검색할 때 페이지네이션과 카테고리 필터링이 적용된다")
    @Transactional
    void searchAuctionsWithPaginationAndCategoryFilter() {
        // 여러 경매 생성을 위한 준비
        long memberId = 1L;

        // 첫 번째 경매 생성
        AuctionCreateRequest firstAuction = new AuctionCreateRequest(
                "나이키 에어맥스",
                "나이키 에어맥스 신발입니다",
                "http://deal4U.com/thumbnail1.jpg",
                List.of("http://deal4U.com/image1.jpg"),
                1L, // 카테고리 ID
                "서울시 강남구",
                "상세주소1",
                "123456",
                "010-1111-1111",
                THREE,
                BigDecimal.valueOf(50000L),
                null
        );

        // 두 번째 경매 생성 (다른 카테고리)
        AuctionCreateRequest secondAuction = new AuctionCreateRequest(
                "아디다스 운동화",
                "아디다스 운동화입니다",
                "http://deal4U.com/thumbnail2.jpg",
                List.of("http://deal4U.com/image2.jpg"),
                2L, // 다른 카테고리 ID
                "서울시 서초구",
                "상세주소2",
                "234567",
                "010-2222-2222",
                THREE,
                BigDecimal.valueOf(40000L),
                null
        );

        // 경매 등록
        authRequest(memberId)
                .body(firstAuction)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.CREATED.value());

        authRequest(memberId)
                .body(secondAuction)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.CREATED.value());

        // 카테고리 ID로 필터링 테스트
        List<AuctionListResponse> categoryFilteredResponses = request()
                .param("categoryId", 1L)
                .get("/api/v1/auctions")
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        // 카테고리 ID가 1인 경매만 조회되는지 확인
        assertThat(categoryFilteredResponses).isNotEmpty();
        assertThat(categoryFilteredResponses.stream()
                .allMatch(auction -> auction.category().categoryId() == 1L))
                .isTrue();

        // 페이지네이션 테스트
        List<AuctionListResponse> paginatedResponses = request()
                .param("page", 0)
                .param("size", 1)
                .get("/api/v1/auctions")
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        // 페이지 크기가 1인지 확인
        assertThat(paginatedResponses).hasSize(1);
    }

    @Test
    @DisplayName("경매를 다양한 정렬 순서로 조회할 수 있다")
    @Transactional
    void searchAuctionsWithDifferentSortOrders() {
        // 여러 경매 생성을 위한 준비
        long memberId = 1L;

        // 첫 번째 경매 생성
        AuctionCreateRequest firstAuction = new AuctionCreateRequest(
                "인수테스트 첫 번째 상품",
                "첫 번째 상품 설명",
                "http://deal4U.com/thumbnail1.jpg",
                List.of("http://deal4U.com/image1.jpg"),
                1L,
                "서울시 강남구",
                "상세주소1",
                "123456",
                "010-1111-1111",
                THREE,
                BigDecimal.valueOf(10000L),
                null
        );

        // 두 번째 경매 생성
        AuctionCreateRequest secondAuction = new AuctionCreateRequest(
                "인수테스트 두 번째 상품",
                "두 번째 상품 설명",
                "http://deal4U.com/thumbnail2.jpg",
                List.of("http://deal4U.com/image2.jpg"),
                1L,
                "서울시 서초구",
                "상세주소2",
                "234567",
                "010-2222-2222",
                THREE,
                BigDecimal.valueOf(20000L),
                null
        );

        // 경매 등록
        authRequest(memberId)
                .body(firstAuction)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.CREATED.value());

        authRequest(memberId)
                .body(secondAuction)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.CREATED.value());

        // 최신순 정렬 테스트
        List<AuctionListResponse> latestResponses = request()
                .param("order", "latest")
                .param("keyword", "인수테스트")
                .get("/api/v1/auctions")
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        // 최신순으로 정렬되었는지 확인 (두 번째 상품이 더 최신)
        assertThat(latestResponses).isNotEmpty();

        // 마감임박순 정렬 테스트
        List<AuctionListResponse> timeoutResponses = request()
                .param("order", "timeout")
                .param("keyword", "인수테스트")
                .get("/api/v1/auctions")
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        // 마감임박순으로 정렬되었는지 확인
        assertThat(timeoutResponses).isNotEmpty();
    }

    @Test
    @DisplayName("경매 이미지를 업로드할 수 있다")
    @Transactional
    void uploadAuctionImage() throws IOException {
        // 이미지 파일 모의 생성
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Mockito.when(fileSaver.save(any(), any())).thenAnswer(args -> {
            Object value = args.getArgument(0);
            return testUrl((SavePath) value);
        });

        // 이미지 업로드 테스트
        AuctionImageUrlResponse response = authRequest(1L)
                .multiPart("image", imageFile.getOriginalFilename(), imageFile.getBytes(),
                        imageFile.getContentType())
                .when()
                .post("/api/v1/auctions/images")
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(AuctionImageUrlResponse.class);

        // 응답에 URL이 포함되어 있는지 확인
        assertThat(response).isNotNull();
        assertThat(response.imageUrls().getFirst()).isNotEmpty();
    }

    @Test
    @DisplayName("즉시 구매가가 있는 경매를 생성하고 조회할 수 있다")
    @Transactional
    void createAuctionWithInstantBidPrice() {
        long memberId = 1L;
        BigDecimal startingPrice = BigDecimal.valueOf(10000L);
        BigDecimal instantBidPrice = BigDecimal.valueOf(50000L);

        // 즉시 구매가가 있는 경매 생성
        AuctionCreateRequest auctionCreateRequest = new AuctionCreateRequest(
                "즉시 구매 가능 상품",
                "즉시 구매가 설정된 상품입니다",
                "http://deal4U.com/thumbnail.jpg",
                List.of("http://deal4U.com/image.jpg"),
                memberId,
                "서울시 강남구",
                "상세주소",
                "123456",
                "010-1111-1111",
                THREE,
                startingPrice,
                instantBidPrice  // 즉시 구매가 설정
        );

        // 경매 등록
        authRequest(memberId)
                .body(auctionCreateRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/api/v1/auctions")
                .then().assertThat().statusCode(HttpStatus.CREATED.value());

        // 경매 조회
        List<AuctionListResponse> responses = request()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/api/v1/auctions?keyword={keyword}", auctionCreateRequest.productName())
                .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("content", AuctionListResponse.class);

        assertThat(responses).hasSize(1);

        // 상세 조회
        AuctionDetailResponse detailResponse = request()
                .get("/api/v1/auctions/{id}/description", responses.getFirst().auctionId())
                .then().assertThat().statusCode(HttpStatus.OK.value())
                .extract().body().as(AuctionDetailResponse.class);

        // 시작가와 즉시 구매가 확인
        assertThat(detailResponse.startingPrice().longValue()).isEqualTo(startingPrice.longValue());
        assertThat(detailResponse.instantBidPrice().longValue()).isEqualTo(
                instantBidPrice.longValue());
    }

    private URL testUrl(SavePath savePath) {

        try {
            URI uri = new URI("http", null, "localhost", 8080, "/" + savePath.fullPath(), null,
                    null);
            return uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
