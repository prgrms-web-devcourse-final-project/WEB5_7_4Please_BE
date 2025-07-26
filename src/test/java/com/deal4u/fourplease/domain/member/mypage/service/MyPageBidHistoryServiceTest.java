package com.deal4u.fourplease.domain.member.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MyBidBase;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
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
        Page<MyBidBase> emptyPage = Page.empty();
        given(bidRepository.findMyBidHistory(memberId, pageable)).willReturn(emptyPage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("진행중인 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenOpenAuctionReturnsProgressStatus() {
        // given
        MyBidBase myBidBase = createBidHistoryBase(
                AuctionStatus.OPEN, // 경매 상태
                false,              // 낙찰 여부 (진행 중이므로 false)
                null,               // 정산 상태 (진행 중이므로 null)
                null,               // 배송 상태 (진행 중이므로 null)
                null,               // 결제 마감 기한 (진행 중이므로 null)
                BigDecimal.valueOf(15000) // 최고 입찰가
        );
        Page<MyBidBase> basePage = new PageImpl<>(List.of(myBidBase), pageable, 1);

        given(bidRepository.findMyBidHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("OPEN");
        assertThat(result.getContent().getFirst().highestBidPrice()).isEqualTo(
                BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("낙찰된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenSuccessfulBidReturnsWinningStatus() {
        // given
        MyBidBase myBidBase = createBidHistoryBase(
                AuctionStatus.CLOSE,
                true,
                SettlementStatus.PENDING,
                null,
                now.plusDays(1),
                BigDecimal.valueOf(20000) // 최고 입찰가
        );
        Page<MyBidBase> basePage = new PageImpl<>(List.of(myBidBase),pageable, 1);

        given(bidRepository.findMyBidHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("PENDING");
        assertThat(result.getContent().getFirst().paymentDeadline()).isNotEmpty();
    }

    @Test
    @DisplayName("결제 완료된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenPaymentSuccessReturnsPaymentCompleteStatus() {
        // given
        MyBidBase myBidBase = createBidHistoryBase(
                AuctionStatus.CLOSE,
                true,
                SettlementStatus.SUCCESS,
                null,
                now.plusDays(1),
                BigDecimal.valueOf(20000) // 최고 입찰가
        );

        Page<MyBidBase> basePage = new PageImpl<>(List.of(myBidBase),pageable, 1);

        given(bidRepository.findMyBidHistory(memberId, pageable)).willReturn(basePage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(
                Member.builder().memberId(memberId).build(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo("SUCCESS");
    }

//    @Test
//    @DisplayName("배송중인 경매의 입찰 내역을 조회한다")
//    void getMyBidHistoryWhenInTransitReturnsShippingStatus() {
//        // given
//        MyPageBidHistoryBase base = createBidHistoryBase("CLOSE", true);
//        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));
//
//        SettlementInfo settlement = new SettlementInfo(1L, "SUCCESS", now.plusDays(1), "INTRANSIT");
//
//        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
//        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
//                List.of(settlement));
//        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
//                List.of(new HighestBidInfo(1L, 15000.0))
//        );
//
//        // when
//        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(
//                Member.builder().memberId(memberId).build(), pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().getFirst().status()).isEqualTo("INTRANSIT");
//    }
//
//    @Test
//    @DisplayName("패찰된 경매의 입찰 내역을 조회한다")
//    void getMyBidHistoryWhenFailedBidReturnsFailedStatus() {
//        // given
//        MyPageBidHistoryBase base = createBidHistoryBase("CLOSE", false);
//        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));
//
//        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
//        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
//                List.of());
//        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
//                List.of(new HighestBidInfo(1L, 15000.0))
//        );
//
//        // when
//        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(
//                Member.builder().memberId(memberId).build(), pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().getFirst().status()).isEqualTo("CLOSE");
//    }
//
//    @Test
//    @DisplayName("실패한 경매의 입찰 내역을 조회한다")
//    void getMyBidHistoryWhenAuctionFailedReturnsFailedStatus() {
//        // given
//        MyPageBidHistoryBase base = createBidHistoryBase("FAIL", false);
//        Page<MyPageBidHistoryBase> basePage = new PageImpl<>(List.of(base));
//
//        given(bidRepository.findMyBidHistoryBase(memberId, pageable)).willReturn(basePage);
//        given(bidRepository.findSettlementInfoByAuctionIds(eq(memberId), any())).willReturn(
//                List.of());
//        given(bidRepository.findHighestBidInfoByAuctionIds(any())).willReturn(
//                List.of(new HighestBidInfo(1L, 15000.0))
//        );
//
//        // when
//        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(
//                Member.builder().memberId(memberId).build(), pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().getFirst().status()).isEqualTo("FAIL");
//    }

    private MyBidBase createBidHistoryBase(
            AuctionStatus auctionStatus,
            Boolean isSuccessful,
            SettlementStatus settlementStatus,
            ShipmentStatus shipmentStatus,
            LocalDateTime paymentDeadline,
            BigDecimal highestBidPrice) {

        Member dummySellerMember = Member.builder().memberId(100L).nickName("테스트판매자").build();
        // Seller 클래스가 Member를 인자로 받는 생성자를 가진다고 가정
        Seller dummySeller = Seller.create(dummySellerMember);

        return new MyBidBase(
                1L,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(20000),
                auctionStatus,
                dummySeller,
                "테스트 상품",
                "thumbnail.jpg",
                101L,
                BigDecimal.valueOf(15000),
                now.minusMinutes(1),
                isSuccessful,
                settlementStatus,
                paymentDeadline,
                shipmentStatus,
                highestBidPrice
        );
    }
}
