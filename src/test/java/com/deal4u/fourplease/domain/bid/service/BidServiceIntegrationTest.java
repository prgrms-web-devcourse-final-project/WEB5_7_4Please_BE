package com.deal4u.fourplease.domain.bid.service;

import com.deal4u.fourplease.config.BidWebSocketHandler;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BidServiceIntegrationTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private BidWebSocketHandler bidWebSocketHandler;

    private final Long memberIdA = 2L;
    private final Long memberIdB = 3L;
    // `data.sql`로 추가되지 않은 존재하지 않는 유저
    private final Long memberIdC = 4L;

    private final Long auctionId = 1L;

    private Bid initialBid;

    @BeforeEach
    @Transactional
    void setUp() {
        int initialBidPrice = 150_000;
        BidRequest initialRequest = new BidRequest(auctionId, initialBidPrice);
        // H2 DB에 실제 데이터 등록
        bidService.createBid(memberIdA, initialRequest);
        Member member = memberRepository.findById(memberIdA).orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);
        Bidder bidder = new Bidder(member);
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        this.initialBid = bidRepository.findByAuctionAndBidder(auction, bidder).orElseThrow(ErrorCode.BID_NOT_FOUND::toException);
    }

    @Test
    @DisplayName("신규 입찰 (기존 최대 입찰가보다 낮은 경우)")
    void createBid_Lower_Price_IntegrationTest() {
        // memberId = 1인 유저가 이미 입찰되어 있기 때문에 (입찰자`A`)
        // memberId = 2인 유저로 입찰 (입찰자`B`)
        BidRequest request = new BidRequest(auctionId, 120_000);
        bidService.createBid(memberIdB, request);
    }

    @Test
    @DisplayName("신규 입찰 (기존 최대 입찰가보다 높은 경우)")
    void createBid_Higher_Price_IntegrationTest() {
        // memberId = 1인 유저가 이미 입찰되어 있기 때문에 (입찰자`A`)
        // memberId = 2인 유저로 입찰 (입찰자`B`)
        BidRequest request = new BidRequest(auctionId, 180_000);
        bidService.createBid(memberIdB, request);
    }

    @Test
    @DisplayName("입찰 갱신 성공 (기본 입찰가보다 높은 금액)")
    void updateBid_Higher_Price_IntegrationTest() {
        // 1. 입찰자`A`의 신규 입찰 객체 생성
        BidRequest updateRequest = new BidRequest(auctionId, 180_000);

        // 2. DB 상에 존재하는 입찰자`A`의 입찰을 갱신
        bidService.createBid(memberIdB, updateRequest);
    }

    @Test
    @DisplayName("입찰 갱신 실패 (기본 입찰가보다 낮은 금액)")
    void updateBid_Lower_Price_IntegrationTest() {
        // 1. 입찰자`A`의 신규 입찰 객체 생성
        BidRequest updateRequest = new BidRequest(auctionId, 100_000);

        // 2. BID_FORBIDDEN_PRICE `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.createBid(memberIdA, updateRequest);
        });
        // 3. 발생한 `Exception`이 `BID_FORBIDDEN_PRICE`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.BID_FORBIDDEN_PRICE.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.BID_FORBIDDEN_PRICE.getStatus());
    }

    @Test
    @DisplayName("입찰 갱신 실패 (입찰 유저가 존재하지 않는 경우)")
    void updateBid_Not_Matched_Member_IntegrationTest() {
        // 1. MEMBER_NOT_FOUND `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.deleteBid(memberIdC, auctionId);
        });
        // 2. 발생한 `Exception`이 `MEMBER_NOT_FOUND`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
    }

    @Test
    @DisplayName("입찰 취소 성공")
    void deleteBid_IntegrationTest() {
        bidService.deleteBid(memberIdA, initialBid.getBidId());
    }

    @Test
    @DisplayName("입찰 취소 실패 (유저가 존재하지 않는 경우)")
    void deleteBid_Not_Matched_Member_IntegrationTest() {
        // 1. MEMBER_NOT_FOUND `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.deleteBid(memberIdC, initialBid.getBidId());
        });
        // 2. 발생한 `Exception`이 `MEMBER_NOT_FOUND`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
    }

    @Test
    @DisplayName("입찰 취소 실패 (유저가 일치하지 않는 경우)")
    void deleteBid_Not_Matched_Bid_IntegrationTest() {
        // 1. BID_NOT_FOUND `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.deleteBid(memberIdB, initialBid.getBidId());
        });
        // 2. 발생한 `Exception`이 `BID_NOT_FOUND`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.BID_NOT_FOUND.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.BID_NOT_FOUND.getStatus());
    }

}