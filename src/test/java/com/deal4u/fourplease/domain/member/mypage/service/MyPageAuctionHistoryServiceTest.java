package com.deal4u.fourplease.domain.member.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MyAuctionBase;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageAuctionHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MyPageAuctionHistoryServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private MyPageAuctionHistoryService myPageAuctionHistoryService;

    private Pageable pageable;
    private Long memberId;
    private LocalDateTime now;

    private static final DateTimeFormatter PAYMENT_DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Category 엔티티 인스턴스 추가
    private Category digitalDevicesCategory;
    private Category clothesCategory;
    private Category furnitureCategory;
    private Category booksCategory;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        memberId = 1L;
        now = LocalDateTime.now();

        // 테스트를 위한 Category 엔티티 인스턴스 초기화
        digitalDevicesCategory = new Category(1L, "디지털 기기");
        clothesCategory = new Category(2L, "의류");
        furnitureCategory = new Category(3L, "가구");
        booksCategory = new Category(4L, "도서");
    }

    @Test
    @DisplayName("경매 내역이 없는 경우 빈 페이지를 반환한다")
    void getMyAuctionHistoryWhenNoHistoryReturnsEmptyPage() {
        // given
        Page<MyAuctionBase> emptyPage = Page.empty();
        given(auctionRepository.findMyAuctionHistory(memberId, pageable)).willReturn(emptyPage);

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("진행 중인 경매 내역을 조회 (현재 최고 입찰가 존재)")
    void getMyAuctionHistoryWhenOpenAuctionWithHighestPrice() {
        // given
        MyAuctionBase myAuctionBase = createMyAuctionBase(
                101L,
                now.minusDays(1),
                now.plusDays(1),
                BigDecimal.valueOf(50000),
                AuctionStatus.OPEN,
                "테스트 상품1",
                "thumb1.jpg",
                digitalDevicesCategory,
                null, // successfulBidId
                null, // bidderName
                null, // successfulBidPrice
                5, // bidCount
                BigDecimal.valueOf(45000), // currentHighestBidPrice
                null, // paymentDeadline
                now.minusDays(2) // createdAt
        );
        Page<MyAuctionBase> basePage = new PageImpl<>(List.of(myAuctionBase), pageable, 1);

        given(auctionRepository.findMyAuctionHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(101L);
        assertThat(history.name()).isEqualTo("테스트 상품1");
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.valueOf(45000)); // highestPrice
        assertThat(history.instantPrice()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(history.bidCount()).isEqualTo(5);
        assertThat(history.bidderName()).isNull(); // Open 상태이므로 낙찰자 없음
        assertThat(history.paymentDeadline()).isEmpty(); // Open 상태이므로 결제 마감 기한 없음
        assertThat(history.status()).isEqualTo(AuctionStatus.OPEN);
    }

    @Test
    @DisplayName("진행 중인 경매 내역을 조회한다 (입찰가 없음)")
    void getMyAuctionHistoryWhenOpenAuctionNoBid() {
        // given
        MyAuctionBase myAuctionBase = createMyAuctionBase(
                102L,
                now.minusHours(5),
                now.plusHours(10),
                BigDecimal.valueOf(10000),
                AuctionStatus.OPEN,
                "입찰 없는 상품",
                "thumb2.jpg",
                clothesCategory, // Category 엔티티 인스턴스 사용
                null,
                null,
                null,
                0, // bidCount
                null, // currentHighestBidPrice (입찰 없으므로 null)
                null,
                now.minusHours(6)
        );
        Page<MyAuctionBase> basePage = new PageImpl<>(List.of(myAuctionBase), pageable, 1);

        given(auctionRepository.findMyAuctionHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(102L);
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.ZERO); // 입찰가 없으므로 0
        assertThat(history.bidCount()).isZero();
        assertThat(history.bidderName()).isNull();
        assertThat(history.paymentDeadline()).isEmpty();
        assertThat(history.status()).isEqualTo(AuctionStatus.OPEN);
        assertThat(history.category().getName()).isEqualTo(clothesCategory.getName()); // Category 이름 검증
    }

    @Test
    @DisplayName("종료된 경매 내역을 조회한다 (낙찰 성공)")
    void getMyAuctionHistoryWhenClosedAuctionSuccessfulBid() {
        // given
        LocalDateTime paymentDeadline = now.plusDays(2);
        MyAuctionBase myAuctionBase = createMyAuctionBase(
                103L,
                now.minusDays(5),
                now.minusDays(1), // 경매 종료
                BigDecimal.valueOf(100000),
                AuctionStatus.CLOSE, // 경매 상태는 CLOSE
                "낙찰된 상품",
                "thumb3.jpg",
                furnitureCategory, // Category 엔티티 인스턴스 사용
                201L, // successfulBidId
                "성공입찰자", // bidderName
                BigDecimal.valueOf(95000), // successfulBidPrice
                10,
                BigDecimal.valueOf(95000), // currentHighestBidPrice (성공 입찰가와 동일)
                paymentDeadline, // paymentDeadline 존재
                now.minusDays(6)
        );
        Page<MyAuctionBase> basePage = new PageImpl<>(List.of(myAuctionBase), pageable, 1);

        given(auctionRepository.findMyAuctionHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(103L);
        assertThat(history.name()).isEqualTo("낙찰된 상품");
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.valueOf(95000));
        assertThat(history.bidderName()).isEqualTo("성공입찰자");
        assertThat(history.paymentDeadline()).isEqualTo(paymentDeadline.format(PAYMENT_DEADLINE_FORMAT));
        assertThat(history.status()).isEqualTo(AuctionStatus.CLOSE); // AuctionStatus 그대로
        assertThat(history.category().getName()).isEqualTo(furnitureCategory.getName()); // Category 이름 검증
    }


    @Test
    @DisplayName("폐찰된 경매 내역을 조회한다")
    void getMyAuctionHistoryWhenFailedAuction() {
        // given
        MyAuctionBase myAuctionBase = createMyAuctionBase(
                104L,
                now.minusDays(3),
                now.minusHours(1), // 경매 종료
                BigDecimal.valueOf(20000),
                AuctionStatus.FAIL, // 유찰 상태
                "폐찰된 상품",
                "thumb4.jpg",
                booksCategory, // Category 엔티티 인스턴스 사용
                null,
                null,
                null,
                3,
                BigDecimal.valueOf(18000), // 유찰되었더라도 최고 입찰가는 있을 수 있음
                null,
                now.minusDays(4)
        );
        Page<MyAuctionBase> basePage = new PageImpl<>(List.of(myAuctionBase), pageable, 1);

        given(auctionRepository.findMyAuctionHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(104L);
        assertThat(history.name()).isEqualTo("폐찰된 상품");
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.valueOf(18000));
        assertThat(history.bidderName()).isNull();
        assertThat(history.paymentDeadline()).isEmpty();
        assertThat(history.status()).isEqualTo(AuctionStatus.FAIL);
        assertThat(history.category().getName()).isEqualTo(booksCategory.getName()); // Category 이름 검증
    }

    @Test
    @DisplayName("여러 경매 내역이 페이지네이션되어 반환된다")
    void getMyAuctionHistoryWhenMultipleAuctionsReturnsPaginated() {
        // given
        MyAuctionBase auction1 = createMyAuctionBase(
                105L, now.minusDays(1), now.plusDays(1), BigDecimal.valueOf(10000), AuctionStatus.OPEN,
                "상품5", "t5.jpg", clothesCategory, null, null, null, 2, BigDecimal.valueOf(9000), null, now.minusDays(1) // Category 인스턴스
        );
        MyAuctionBase auction2 = createMyAuctionBase(
                106L, now.minusDays(5), now.minusDays(1), BigDecimal.valueOf(50000), AuctionStatus.CLOSE,
                "상품6", "t6.jpg", digitalDevicesCategory, 301L, "낙찰자2", BigDecimal.valueOf(45000), 7, BigDecimal.valueOf(45000), now.plusDays(2), now.minusDays(5) // Category 인스턴스
        );
        MyAuctionBase auction3 = createMyAuctionBase(
                107L, now.minusDays(10), now.minusDays(2), BigDecimal.valueOf(2000), AuctionStatus.FAIL,
                "상품7", "t7.jpg", booksCategory, null, null, null, 1, BigDecimal.valueOf(1500), null, now.minusDays(10) // Category 인스턴스
        );

        List<MyAuctionBase> auctionList = List.of(auction1, auction2, auction3);
        Page<MyAuctionBase> basePage = new PageImpl<>(auctionList, pageable, auctionList.size());

        given(auctionRepository.findMyAuctionHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).auctionId()).isEqualTo(105L);
        assertThat(result.getContent().get(1).auctionId()).isEqualTo(106L);
        assertThat(result.getContent().get(2).auctionId()).isEqualTo(107L);
        // Category 이름 검증 예시
        assertThat(result.getContent().get(0).category().getName()).isEqualTo(clothesCategory.getName());
    }

    /**
     * MyAuctionBase 객체를 생성하기 위한 헬퍼 메서드
     */
    private MyAuctionBase createMyAuctionBase(
            Long auctionId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal instantPrice,
            AuctionStatus status,
            String name,
            String thumbnailUrl,
            Category category,
            Long bidId, // successfulBid.bidId
            String bidderName, // successfulBidMember.nickName
            BigDecimal successfulBidPrice, // successfulBid.price
            Integer bidCount, // bidCountInfo.totalBidCount
            BigDecimal currentHighestBidPrice, // maxBid.highestPrice
            LocalDateTime paymentDeadline, // s.paymentDeadline
            LocalDateTime createdAt // a.createdAt
    ) {
        return new MyAuctionBase(
                auctionId,
                startTime,
                endTime,
                instantPrice,
                status,
                name,
                thumbnailUrl,
                category,
                bidId,
                bidderName,
                successfulBidPrice,
                bidCount,
                currentHighestBidPrice,
                paymentDeadline,
                createdAt
        );
    }
}
