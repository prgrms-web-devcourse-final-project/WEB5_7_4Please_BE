package com.deal4u.fourplease.domain.member.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.CountBid;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBid;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageAuctionHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementDeadline;
import com.deal4u.fourplease.domain.member.mypage.dto.SuccessfulBidder;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import jakarta.persistence.Tuple;
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

    @Mock
    private BidRepository bidRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private Tuple tuple;

    @InjectMocks
    private MyPageAuctionHistoryService myPageAuctionHistoryService;

    private Pageable pageable;
    private Long memberId;
    private LocalDateTime now;
    private Member member;

    private static final DateTimeFormatter PAYMENT_DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Category digitalDevicesCategory;
    private Category clothesCategory;
    private Category furnitureCategory;
    private Category booksCategory;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        memberId = 1L;
        now = LocalDateTime.now();
        member = Member.builder().memberId(memberId).build();

        digitalDevicesCategory = new Category(1L, "디지털 기기");
        clothesCategory = new Category(2L, "의류");
        furnitureCategory = new Category(3L, "가구");
        booksCategory = new Category(4L, "도서");
    }

    @Test
    @DisplayName("경매 내역이 없는 경우 빈 페이지를 반환한다")
    void getMyAuctionHistoryWhenNoHistoryReturnsEmptyPage() {
        // given
        Page<Tuple> emptyPage = Page.empty();
        given(auctionRepository.findAllAuctionHistoryByMemberId(memberId, pageable)).willReturn(
                emptyPage);

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                member, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("진행 중인 경매 내역을 조회 (현재 최고 입찰가 존재)")
    void getMyAuctionHistoryWhenOpenAuctionWithHighestPrice() {
        // given
        Long auctionId = 101L;
        String orderId = "ORDER-2024-001";
        setupTupleMock(auctionId, "thumb1.jpg", digitalDevicesCategory, "테스트 상품1",
                BigDecimal.valueOf(50000), now.plusDays(1), now.minusDays(1), AuctionStatus.OPEN,
                orderId);

        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(45000));
        CountBid countBid = new CountBid(auctionId, 5L);
        SuccessfulBidder successfulBidder = new SuccessfulBidder(auctionId, null, null);

        given(auctionRepository.findAllAuctionHistoryByMemberId(memberId, pageable)).willReturn(
                tuplePage);
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(highestBid));
        given(bidRepository.findCountBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(countBid));
        given(bidRepository.findSuccessfulBidderForAuctionIds(List.of(auctionId))).willReturn(
                List.of(successfulBidder));
        given(settlementRepository.findSettlementDeadlinesByAuctionIds(
                List.of(auctionId))).willReturn(List.of());

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(101L);
        assertThat(history.name()).isEqualTo("테스트 상품1");
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.valueOf(45000));
        assertThat(history.instantPrice()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(history.bidCount()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(history.bidderName()).isEqualTo("낙찰자 없음");
        assertThat(history.paymentDeadline()).isEmpty();
        assertThat(history.status()).isEqualTo(AuctionStatus.OPEN);
        assertThat(history.orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("진행 중인 경매 내역을 조회한다 (입찰가 없음)")
    void getMyAuctionHistoryWhenOpenAuctionNoBid() {
        // given
        Long auctionId = 102L;
        String orderId = null;
        setupTupleMock(auctionId, "thumb2.jpg", clothesCategory, "입찰 없는 상품",
                BigDecimal.valueOf(10000), now.plusHours(10), now.minusHours(5),
                AuctionStatus.OPEN, orderId);

        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.ZERO);
        CountBid countBid = new CountBid(auctionId, 0L);
        SuccessfulBidder successfulBidder = new SuccessfulBidder(auctionId, null, null);

        given(auctionRepository.findAllAuctionHistoryByMemberId(memberId, pageable)).willReturn(
                tuplePage);
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(highestBid));
        given(bidRepository.findCountBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(countBid));
        given(bidRepository.findSuccessfulBidderForAuctionIds(List.of(auctionId))).willReturn(
                List.of(successfulBidder));
        given(settlementRepository.findSettlementDeadlinesByAuctionIds(
                List.of(auctionId))).willReturn(List.of());

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(102L);
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(history.bidCount()).isEqualTo(BigDecimal.ZERO);
        assertThat(history.bidderName()).isEqualTo("낙찰자 없음");
        assertThat(history.paymentDeadline()).isEmpty();
        assertThat(history.status()).isEqualTo(AuctionStatus.OPEN);
        assertThat(history.category().getName()).isEqualTo(clothesCategory.getName());
        assertThat(history.orderId()).isNull();
    }

    @Test
    @DisplayName("종료된 경매 내역을 조회한다 (낙찰 성공)")
    void getMyAuctionHistoryWhenClosedAuctionSuccessfulBid() {
        // given
        Long auctionId = 103L;
        String orderId = "ORDER-2024-002";
        LocalDateTime paymentDeadline = now.plusDays(2);

        setupTupleMock(auctionId, "thumb3.jpg", furnitureCategory, "낙찰된 상품",
                BigDecimal.valueOf(100000), now.minusDays(1), now.minusDays(5),
                AuctionStatus.CLOSE, orderId);

        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(95000));
        CountBid countBid = new CountBid(auctionId, 10L);
        SuccessfulBidder successfulBidder = new SuccessfulBidder(auctionId, 201L, "성공입찰자");
        SettlementDeadline settlementDeadline = new SettlementDeadline(auctionId, paymentDeadline);

        given(auctionRepository.findAllAuctionHistoryByMemberId(memberId, pageable)).willReturn(
                tuplePage);
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(highestBid));
        given(bidRepository.findCountBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(countBid));
        given(bidRepository.findSuccessfulBidderForAuctionIds(List.of(auctionId))).willReturn(
                List.of(successfulBidder));
        given(settlementRepository.findSettlementDeadlinesByAuctionIds(
                List.of(auctionId))).willReturn(List.of(settlementDeadline));

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(103L);
        assertThat(history.name()).isEqualTo("낙찰된 상품");
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.valueOf(95000));
        assertThat(history.bidderName()).isEqualTo("성공입찰자");
        assertThat(history.paymentDeadline()).isEqualTo(
                paymentDeadline.format(PAYMENT_DEADLINE_FORMAT));
        assertThat(history.status()).isEqualTo(AuctionStatus.CLOSE);
        assertThat(history.category().getName()).isEqualTo(furnitureCategory.getName());
        assertThat(history.orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("폐찰된 경매 내역을 조회한다")
    void getMyAuctionHistoryWhenFailedAuction() {
        // given
        Long auctionId = 104L;
        String orderId = null;
        setupTupleMock(auctionId, "thumb4.jpg", booksCategory, "폐찰된 상품",
                BigDecimal.valueOf(20000), now.minusHours(1), now.minusDays(3), AuctionStatus.FAIL,
                orderId);

        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(18000));
        CountBid countBid = new CountBid(auctionId, 3L);
        SuccessfulBidder successfulBidder = new SuccessfulBidder(auctionId, null, null);

        given(auctionRepository.findAllAuctionHistoryByMemberId(memberId, pageable)).willReturn(
                tuplePage);
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(highestBid));
        given(bidRepository.findCountBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(countBid));
        given(bidRepository.findSuccessfulBidderForAuctionIds(List.of(auctionId))).willReturn(
                List.of(successfulBidder));
        given(settlementRepository.findSettlementDeadlinesByAuctionIds(
                List.of(auctionId))).willReturn(List.of());

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.auctionId()).isEqualTo(104L);
        assertThat(history.name()).isEqualTo("폐찰된 상품");
        assertThat(history.maxPrice()).isEqualTo(BigDecimal.valueOf(18000));
        assertThat(history.bidderName()).isEqualTo("낙찰자 없음");
        assertThat(history.paymentDeadline()).isEmpty();
        assertThat(history.status()).isEqualTo(AuctionStatus.FAIL);
        assertThat(history.category().getName()).isEqualTo(booksCategory.getName());
        assertThat(history.orderId()).isNull();
    }

    @Test
    @DisplayName("낙찰자 정보가 null인 경우 알 수 없음으로 표시된다")
    void getMyAuctionHistoryWhenSuccessfulBidderIsNull() {
        // given
        Long auctionId = 105L;
        String orderId = "ORDER-2024-003";
        setupTupleMock(auctionId, "thumb5.jpg", digitalDevicesCategory, "낙찰자 미상 상품",
                BigDecimal.valueOf(30000), now.minusDays(1), now.minusDays(2), AuctionStatus.CLOSE,
                orderId);

        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(25000));
        CountBid countBid = new CountBid(auctionId, 5L);
        SuccessfulBidder successfulBidder = new SuccessfulBidder(auctionId, null, null);

        given(auctionRepository.findAllAuctionHistoryByMemberId(memberId, pageable)).willReturn(
                tuplePage);
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(highestBid));
        given(bidRepository.findCountBidsForAuctionIds(List.of(auctionId))).willReturn(
                List.of(countBid));
        given(bidRepository.findSuccessfulBidderForAuctionIds(List.of(auctionId))).willReturn(
                List.of(successfulBidder));
        given(settlementRepository.findSettlementDeadlinesByAuctionIds(
                List.of(auctionId))).willReturn(List.of());

        // when
        PageResponse<MyPageAuctionHistory> result = myPageAuctionHistoryService.getMyAuctionHistory(
                member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageAuctionHistory history = result.getContent().getFirst();
        assertThat(history.bidderName()).isEqualTo("낙찰자 없음");
        assertThat(history.orderId()).isEqualTo(orderId);
    }

    private void setupTupleMock(Long auctionId, String thumbnailUrl, Category category, String name,
                                BigDecimal instantBidPrice, LocalDateTime endTime,
                                LocalDateTime startTime, AuctionStatus status, String orderId) {
        given(tuple.get("auctionId")).willReturn(auctionId);
        given(tuple.get("thumbnailUrl")).willReturn(thumbnailUrl);
        given(tuple.get("category")).willReturn(category);
        given(tuple.get("name")).willReturn(name);
        given(tuple.get("instantBidPrice")).willReturn(instantBidPrice);
        given(tuple.get("endTime")).willReturn(endTime);
        given(tuple.get("startTime")).willReturn(startTime);
        given(tuple.get("status")).willReturn(status);
        given(tuple.get("orderId")).willReturn(orderId);
    }
}
