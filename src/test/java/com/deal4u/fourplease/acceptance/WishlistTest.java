package com.deal4u.fourplease.acceptance;

import static com.deal4u.fourplease.testutil.TestUtils.genAuction;
import static com.deal4u.fourplease.testutil.TestUtils.genAuctionList;
import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("위시리스트 관련 인수 테스트")
@TestInstance(Lifecycle.PER_METHOD)
@Transactional
class WishlistTest extends MockMvcBaseAcceptTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    private Member testMember;
    private Auction testAuction;

    @BeforeEach
    void setUpTestData() {
        testMember = Member.builder()
                .memberId(1L)
                .email("seller@example.com")
                .nickName("유저1")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .provider("provider")
                .build();
        testMember = memberRepository.save(testMember);

        testAuction = genAuction();
        testAuction = auctionRepository.save(testAuction);
    }

    @Test
    @DisplayName("위시리스트가 생성되고 조회된다")
    void createWishlist() {
        WishlistCreateRequest request = new WishlistCreateRequest(testAuction.getAuctionId());

        // 위시리스트 생성
        Long wishlistId = authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(Long.class);

        // 위시리스트가 생성 확인
        assertThat(wishlistId).isNotNull().isPositive();

        // 위시리스트 목록 조회
        PageResponse<WishlistResponse> response = authRequest(testMember.getMemberId())
                .when()
                .get("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<PageResponse<WishlistResponse>>() {
                });

        // 조회 결과 검증
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).wishlistId()).isEqualTo(wishlistId);
        assertThat(response.getContent().get(0).auctionId()).isEqualTo(
                testAuction.getAuctionId());
    }

    @Test
    @DisplayName("위시리스트를 등록하고 취소 시킨다")
    void deleteWishlist() {
        // 위시리스트 생성
        WishlistCreateRequest request = new WishlistCreateRequest(testAuction.getAuctionId());

        Long wishlistId = authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(Long.class);

        // 위시리스트 삭제
        authRequest(testMember.getMemberId())
                .when()
                .delete("/api/v1/wishlist/{wishlistId}", wishlistId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // 위시리스트가 삭제되었는지 확인
        PageResponse<WishlistResponse> response = authRequest(testMember.getMemberId())
                .when()
                .get("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<PageResponse<WishlistResponse>>() {
                });

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();

        // 데이터베이스에서 직접 확인
        Optional<Wishlist> deletedWishlist = wishlistRepository.findById(wishlistId);
        assertThat(deletedWishlist).isEmpty();
    }

    @Test
    @DisplayName("페이징과 정렬로 위시리스트를 조회한다")
    void readWishlistWithPagingAndSorting() {
        // 여러 개의 위시리스트 생성
        List<Auction> auctions = genAuctionList();
        Auction auction1 = auctions.get(0);
        Auction auction2 = auctions.get(1);

        auction1 = auctionRepository.save(auction1);
        auction2 = auctionRepository.save(auction2);

        // 위시리스트 생성
        authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(new WishlistCreateRequest(testAuction.getAuctionId()))
                .post("/api/v1/wishlist");

        authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(new WishlistCreateRequest(auction1.getAuctionId()))
                .post("/api/v1/wishlist");

        authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(new WishlistCreateRequest(auction2.getAuctionId()))
                .post("/api/v1/wishlist");

        // 첫 번째 페이지 조회 (최신순, 페이지 크기 2)
        PageResponse<WishlistResponse> firstPage = authRequest(testMember.getMemberId())
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("order", "latest")
                .when()
                .get("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<PageResponse<WishlistResponse>>() {
                });

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);

        // 두 번째 페이지 조회
        PageResponse<WishlistResponse> secondPage = authRequest(testMember.getMemberId())
                .queryParam("page", 1)
                .queryParam("size", 2)
                .queryParam("order", "latest")
                .when()
                .get("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<PageResponse<WishlistResponse>>() {
                });

        assertThat(secondPage.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 위시리스트 삭제 시 404 에러가 발생한다")
    void deleteNonExistentWishlist() {
        Long nonExistentWishlistId = 999L;

        authRequest(testMember.getMemberId())
                .when()
                .delete("/api/v1/wishlist/{wishlistId}", nonExistentWishlistId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("존재하지 않는 경매로 위시리스트 생성 시 에러가 발생한다")
    void createWishlistWithNonExistentAuction() {
        Long nonExistentAuctionId = 999L;
        WishlistCreateRequest request = new WishlistCreateRequest(nonExistentAuctionId);

        authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 위시리스트에 접근할 수 없다")
    void forbiddenAccess() {
        WishlistCreateRequest request = new WishlistCreateRequest(testAuction.getAuctionId());

        // 위시리스트 생성
        request()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        // 위시리스트 조회
        request()
                .when()
                .get("/api/v1/wishlist")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        // 위시리스트 삭제
        request()
                .when()
                .delete("/api/v1/wishlist/{wishlistId}", 1L)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}