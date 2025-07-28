package com.deal4u.fourplease.domain.member.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBid;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementInfo;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
import jakarta.persistence.Tuple;
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

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private Tuple tuple;

    @InjectMocks
    private MyPageBidHistoryService myPageBidHistoryService;

    private Pageable pageable;
    private Long memberId;
    private LocalDateTime now;
    private Member member;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        memberId = 2L;
        now = LocalDateTime.now();
        member = Member.builder().memberId(memberId).build();
    }

    @Test
    @DisplayName("입찰 내역이 없는 경우 빈 페이지를 반환한다")
    void getMyBidHistoryWhenNoBidHistoryReturnsEmptyPage() {
        // given
        Page<Tuple> emptyPage = Page.empty();
        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(emptyPage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("진행중인 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenOpenAuctionReturnsProgressStatus() {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        BigDecimal myBidPrice = BigDecimal.valueOf(15000);

        setupTupleMock(auctionId, bidId, myBidPrice, now);
        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        Auction auction = createAuction(auctionId, AuctionStatus.OPEN);
        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(20000));

        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(tuplePage);
        given(auctionRepository.findByAuctionIdIn(List.of(auctionId))).willReturn(List.of(auction));
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(List.of(highestBid));
        given(bidRepository.findSettlementInfoByAuctionIds(memberId, List.of(auctionId))).willReturn(List.of());

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageBidHistory history = result.getContent().getFirst();
        assertThat(history.status()).isEqualTo("OPEN");
        assertThat(history.highestBidPrice()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(history.myBidPrice()).isEqualTo(myBidPrice);
    }

    @Test
    @DisplayName("낙찰된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenSuccessfulBidReturnsWinningStatus() {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        BigDecimal myBidPrice = BigDecimal.valueOf(15000);
        LocalDateTime paymentDeadline = now.plusDays(1);

        setupTupleMock(auctionId, bidId, myBidPrice, now);
        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        Auction auction = createAuction(auctionId, AuctionStatus.CLOSE);
        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(20000));
        SettlementInfo settlementInfo = new SettlementInfo(auctionId, SettlementStatus.PENDING, paymentDeadline, null);

        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(tuplePage);
        given(auctionRepository.findByAuctionIdIn(List.of(auctionId))).willReturn(List.of(auction));
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(List.of(highestBid));
        given(bidRepository.findSettlementInfoByAuctionIds(memberId, List.of(auctionId))).willReturn(List.of(settlementInfo));

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageBidHistory history = result.getContent().getFirst();
        assertThat(history.status()).isEqualTo("PENDING");
        assertThat(history.paymentDeadline()).isNotEmpty();
    }

    @Test
    @DisplayName("결제 완료된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenPaymentSuccessReturnsPaymentCompleteStatus() {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        BigDecimal myBidPrice = BigDecimal.valueOf(15000);
        LocalDateTime paymentDeadline = now.plusDays(1);

        setupTupleMock(auctionId, bidId, myBidPrice, now);
        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        Auction auction = createAuction(auctionId, AuctionStatus.CLOSE);
        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(20000));
        SettlementInfo settlementInfo = new SettlementInfo(auctionId, SettlementStatus.SUCCESS, paymentDeadline, null);

        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(tuplePage);
        given(auctionRepository.findByAuctionIdIn(List.of(auctionId))).willReturn(List.of(auction));
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(List.of(highestBid));
        given(bidRepository.findSettlementInfoByAuctionIds(memberId, List.of(auctionId))).willReturn(List.of(settlementInfo));

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageBidHistory history = result.getContent().getFirst();
        assertThat(history.status()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("배송중인 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenInTransitReturnsShippingStatus() {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        BigDecimal myBidPrice = BigDecimal.valueOf(15000);
        LocalDateTime paymentDeadline = now.plusDays(1);

        setupTupleMock(auctionId, bidId, myBidPrice, now);
        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        Auction auction = createAuction(auctionId, AuctionStatus.CLOSE);
        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(20000));
        SettlementInfo settlementInfo = new SettlementInfo(auctionId, SettlementStatus.SUCCESS, paymentDeadline, ShipmentStatus.INTRANSIT);

        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(tuplePage);
        given(auctionRepository.findByAuctionIdIn(List.of(auctionId))).willReturn(List.of(auction));
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(List.of(highestBid));
        given(bidRepository.findSettlementInfoByAuctionIds(memberId, List.of(auctionId))).willReturn(List.of(settlementInfo));

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageBidHistory history = result.getContent().getFirst();
        assertThat(history.status()).isEqualTo("INTRANSIT");
    }

    @Test
    @DisplayName("배송 완료된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenDeliveredReturnsDeliveredStatus() {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        BigDecimal myBidPrice = BigDecimal.valueOf(15000);
        LocalDateTime paymentDeadline = now.plusDays(1);

        setupTupleMock(auctionId, bidId, myBidPrice, now);
        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        Auction auction = createAuction(auctionId, AuctionStatus.CLOSE);
        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(20000));
        SettlementInfo settlementInfo = new SettlementInfo(auctionId, SettlementStatus.SUCCESS, paymentDeadline, ShipmentStatus.DELIVERED);

        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(tuplePage);
        given(auctionRepository.findByAuctionIdIn(List.of(auctionId))).willReturn(List.of(auction));
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(List.of(highestBid));
        given(bidRepository.findSettlementInfoByAuctionIds(memberId, List.of(auctionId))).willReturn(List.of(settlementInfo));

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageBidHistory history = result.getContent().getFirst();
        assertThat(history.status()).isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("패찰된 경매의 입찰 내역을 조회한다")
    void getMyBidHistoryWhenFailedBidReturnsFailedStatus() {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        BigDecimal myBidPrice = BigDecimal.valueOf(15000);

        setupTupleMock(auctionId, bidId, myBidPrice, now);
        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        Auction auction = createAuction(auctionId, AuctionStatus.FAIL);
        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(20000));

        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(tuplePage);
        given(auctionRepository.findByAuctionIdIn(List.of(auctionId))).willReturn(List.of(auction));
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(List.of(highestBid));
        given(bidRepository.findSettlementInfoByAuctionIds(memberId, List.of(auctionId))).willReturn(List.of());

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageBidHistory history = result.getContent().getFirst();
        assertThat(history.status()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("종료된 경매에서 낙찰되지 않은 경우 FAIL 상태를 반환한다")
    void getMyBidHistoryWhenClosedAuctionWithNoSettlementReturnsFailStatus() {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        BigDecimal myBidPrice = BigDecimal.valueOf(15000);

        setupTupleMock(auctionId, bidId, myBidPrice, now);
        Page<Tuple> tuplePage = new PageImpl<>(List.of(tuple), pageable, 1);

        Auction auction = createAuction(auctionId, AuctionStatus.CLOSE);
        HighestBid highestBid = new HighestBid(auctionId, BigDecimal.valueOf(20000));

        given(bidRepository.findAllBidHistoryByMemberId(memberId, pageable)).willReturn(tuplePage);
        given(auctionRepository.findByAuctionIdIn(List.of(auctionId))).willReturn(List.of(auction));
        given(bidRepository.findHighestBidsForAuctionIds(List.of(auctionId))).willReturn(List.of(highestBid));
        given(bidRepository.findSettlementInfoByAuctionIds(memberId, List.of(auctionId))).willReturn(List.of());

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(member, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        MyPageBidHistory history = result.getContent().getFirst();
        assertThat(history.status()).isEqualTo("FAIL");
    }

    private void setupTupleMock(Long auctionId, Long bidId, BigDecimal myBidPrice, LocalDateTime bidTime) {
        given(tuple.get("auctionId")).willReturn(auctionId);
        given(tuple.get("bidId")).willReturn(bidId);
        given(tuple.get("myBidPrice")).willReturn(myBidPrice);
        given(tuple.get("bidTime")).willReturn(bidTime);
    }

    private Auction createAuction(Long auctionId, AuctionStatus status) {
        Member sellerMember = Member.builder()
                .memberId(100L)
                .nickName("테스트판매자")
                .build();

        Seller seller = Seller.create(sellerMember);

        Product product = Product.builder()
                .name("테스트 상품")
                .thumbnailUrl("thumbnail.jpg")
                .seller(seller)
                .build();

        return Auction.builder()
                .auctionId(auctionId)
                .product(product)
                .status(status)
                .startingPrice(BigDecimal.valueOf(10000))
                .instantBidPrice(BigDecimal.valueOf(30000))
                .build();
    }
}