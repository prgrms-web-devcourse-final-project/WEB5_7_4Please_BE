package com.deal4u.fourplease.domain.member.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBidInfo;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryBase;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementInfo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class MyPageBidHistoryServiceTest {

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private MyPageBidHistoryService myPageBidHistoryService;

    private Pageable pageable;
    private Long memberId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        memberId = 2L;
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("입찰 내역이 없는 경우 빈 페이지를 반환한다")
    void getMyBidHistoryWhenNoBidHistoryReturnsEmptyPage() {
        // given
        Page<MyPageBidHistoryBase> emptyPage = Page.empty();
        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(emptyPage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("진행중인 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenOpenAuctionReturnsProgressStatus() {
        // given
        MyPageBidHistoryBase base = createBidHistoryBase("OPEN", false);
        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));

        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
                List.of());
        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
                List.of(new HighestBidInfo(1L, 15000.0))
        );

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("진행중");
        assertThat(result.getContent().getFirst().highestBidPrice()).isEqualTo(
                BigDecimal.valueOf(15000.0));
    }

    @Test
    @DisplayName("낙찰된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenSuccessfulBidReturnsWinningStatus() {
        // given
        MyPageBidHistoryBase base = createBidHistoryBase("CLOSED", true);
        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));

        SettlementInfo settlement = new SettlementInfo(1L, "PENDING", now.plusDays(1), null);

        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
                List.of(settlement));
        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
                List.of(new HighestBidInfo(1L, 15000.0))
        );

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("낙찰");
        assertThat(result.getContent().getFirst().paymentDeadline()).isNotEmpty();
    }

    @Test
    @DisplayName("결제 완료된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenPaymentSuccessReturnsPaymentCompleteStatus() {
        // given
        MyPageBidHistoryBase base = createBidHistoryBase("CLOSED", true);
        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));

        SettlementInfo settlement = new SettlementInfo(1L, "SUCCESS", now.plusDays(1), "PREPARING");

        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
                List.of(settlement));
        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
                List.of(new HighestBidInfo(1L, 15000.0))
        );

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("결제 완료");
    }

    @Test
    @DisplayName("배송중인 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenInTransitReturnsShippingStatus() {
        // given
        MyPageBidHistoryBase base = createBidHistoryBase("CLOSED", true);
        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));

        SettlementInfo settlement = new SettlementInfo(1L, "SUCCESS", now.plusDays(1), "INTRANSIT");

        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
                List.of(settlement));
        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
                List.of(new HighestBidInfo(1L, 15000.0))
        );

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("배송중");
    }

    @Test
    @DisplayName("패찰된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenFailedBidReturnsFailedStatus() {
        // given
        MyPageBidHistoryBase base = createBidHistoryBase("CLOSED", false);
        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));

        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
                List.of());
        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
                List.of(new HighestBidInfo(1L, 15000.0))
        );

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("경매 종료");
    }

    @Test
    @DisplayName("실패한 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenAuctionFailedReturnsFailedStatus() {
        // given
        MyPageBidHistoryBase base = createBidHistoryBase("FAIL", false);
        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));

        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
                List.of());
        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
                List.of(new HighestBidInfo(1L, 15000.0))
        );

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("패찰");
    }

    private MyPageBidHistoryBase createBidHistoryBase(String status,
                                                      Boolean isSuccessful) {
        return new MyPageBidHistoryBase(
                1L,
                1L,
                "thumbnail.jpg",
                "테스트 상품",
                status,
                10000.0,
                20000.0,
                15000.0,
                isSuccessful,
                now,
                now,
                "판매자"
        );
    }
}
