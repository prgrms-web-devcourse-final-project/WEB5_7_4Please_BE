package com.deal4u.fourplease.domain.bid.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.deal4u.fourplease.config.BidWebSocketHandler;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.scheduler.AuctionCloseJob;
import com.deal4u.fourplease.global.scheduler.AuctionScheduleService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


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

    @Autowired
    AuctionScheduleService auctionScheduleService;

    @Autowired
    AuctionCloseJob auctionCloseJob;

    private final Long memberId1 = 21L;
    private final Long memberId2 = 22L;
    // `data.sql`로 추가되지 않은 존재하지 않는 유저
    private final Long memberIdX = 999L;

    // data.sql 유래의 정상 auctionId
    private final Long auctionId = 1L;
    private final Long auctionId2 = 2L;
    // 존재하지 않는 auctionId
    private final Long auctionIdWrong = 999L;

    private Bid initialBid;

    @BeforeEach
    @Transactional
    void setUp() {
        int initialBidPrice = 150_000;
        BidRequest initialRequest = new BidRequest(auctionId2, initialBidPrice);
        // H2 DB에 실제 데이터 등록
        bidService.createBid(memberId1, initialRequest);
        Member member = memberRepository.findById(memberId1)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);
        Bidder bidder = Bidder.createBidder(member);
        Auction auction = auctionRepository.findById(auctionId2)
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        this.initialBid = bidRepository.findTopByAuctionAndBidderOrderByPriceDesc(auction, bidder)
                .orElseThrow(ErrorCode.BID_NOT_FOUND::toException);
    }

    @Test
    @DisplayName("신규 입찰 (기존 최대 입찰가보다 낮은 경우)")
    void create_bid_lower_price_integrationtest() {
        // memberId = 1인 유저가 이미 입찰되어 있기 때문에 (입찰자`A`)
        // memberId = 2인 유저로 입찰 (입찰자`B`)
        BidRequest request = new BidRequest(auctionId2, 120_000);
        bidService.createBid(memberId2, request);
    }

    @Test
    @DisplayName("신규 입찰 (기존 최대 입찰가보다 높은 경우)")
    void create_bid_higher_price_integrationtest() {
        // memberId = 1인 유저가 이미 입찰되어 있기 때문에 (입찰자`A`)
        // memberId = 2인 유저로 입찰 (입찰자`B`)
        BidRequest request = new BidRequest(auctionId2, 180_000);
        bidService.createBid(memberId2, request);
    }

    @Test
    @DisplayName("입찰 갱신 성공 (자기 자신의 입찰가보다 높은 금액)")
    void update_bid_higher_price_integrationtest() {
        // 1. 입찰자`A`의 신규 입찰 객체 생성
        BidRequest updateRequest = new BidRequest(auctionId2, 180_000);

        // 2. DB 상에 존재하는 입찰자`A`의 입찰을 갱신
        bidService.createBid(memberId1, updateRequest);
    }

    @Test
    @DisplayName("입찰 갱신 실패 (자기 자신의 입찰가보다 낮은 금액)")
    void update_bid_lower_price_integrationtest() {
        // 1. 입찰자`A`의 신규 입찰 객체 생성
        BidRequest updateRequest = new BidRequest(auctionId2, 100_000);

        // 2. BID_FORBIDDEN_PRICE `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.createBid(memberId1, updateRequest);
        });
        // 3. 발생한 `Exception`이 `BID_FORBIDDEN_PRICE`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.BID_FORBIDDEN_PRICE.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.BID_FORBIDDEN_PRICE.getStatus());
    }

    @Test
    @DisplayName("입찰 갱신 실패 (입찰 유저가 존재하지 않는 경우)")
    void update_bid_not_matched_member_integrationtest() {
        // 1. MEMBER_NOT_FOUND `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.deleteBid(memberIdX, auctionId2);
        });
        // 2. 발생한 `Exception`이 `MEMBER_NOT_FOUND`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
    }

    @Test
    @DisplayName("입찰 취소 성공")
    void delete_bid_integrationtest() {
        // 1. 입찰자 A가 입찷한 입찰에 대햇 삭제가 성공하는지 검증
        bidService.deleteBid(memberId1, initialBid.getBidId());


    }

    @Test
    @DisplayName("입찰 취소 실패 (유저가 존재하지 않는 경우)")
    void delete_bid_not_matched_member_integrationtest() {
        // 1. MEMBER_NOT_FOUND `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.deleteBid(memberIdX, initialBid.getBidId());
        });
        // 2. 발생한 `Exception`이 `MEMBER_NOT_FOUND`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
    }

    @Test
    @DisplayName("입찰 취소 실패 (유저가 일치하지 않는 경우)")
    void delete_bid_not_matched_bid_integrationtest() {
        // 1. BID_NOT_FOUND `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.deleteBid(memberId2, initialBid.getBidId());
        });
        // 2. 발생한 `Exception`이 `BID_NOT_FOUND`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.BID_NOT_FOUND.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.BID_NOT_FOUND.getStatus());
    }

    @Test
    @DisplayName("경매 입찰 목록 조회 성공 (첫 페이지(page=0)이 조회됨)")
    void get_bids_by_auction_first_page_integrationtest() {
        // 첫 번째 페이지, Size = 10 ( 조정 가능성이 큽니다.)
        int pageSize = 10;
        Pageable pageable = PageRequest.of(0, pageSize);

        // 1. 입찰 목록 조회
        PageResponse<BidResponse> result = bidService.getBidListForAuction(auctionId, pageable);

        // 2. 페이징 정보 검증
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(19);

        // 3. 입찰 목록의 개수가 동일한지 확인
        assertThat(result.getContent().size()).isLessThanOrEqualTo(pageSize);

        // 4. 정렬 순서 검증 (가격 내림차순 및 입찰 시간 오름차순)
        // data.sql 상의 가장 높은 입찰가는 200,000으로 2건
        // 하지만 입찰자 A가 입찰자 T보다 20초 빠르게 입찰하였기 때문에, 최상위 입찰자는 입찰자 `A`입니다.
        assertThat(result.getContent().getFirst().bidPrice()).isEqualTo(200_000);
        Long memberIdA = 2L;
        assertThat(result.getContent().getFirst().memberId()).isEqualTo(memberIdA);

        // `pageSize`가 10인 경우에 data.sql 상의 정보에 근거하여 10번째 입찰의 금액은 191,000입니다.
        assertThat(result.getContent().getLast().bidPrice()).isEqualTo(192000);
        // `pageSize`가 10인 경우에 data.sql 상의 정보에 근거하여 10번째 입찰자는 입찰자`J`입니다.
        // `pageSize`가 10인 경우에 입찰 내역의 마지막에 해당하는 유저
        Long memberIdI = 10L;
        assertThat(result.getContent().getLast().memberId()).isEqualTo(memberIdI);
    }


    @Test
    @DisplayName("경매 입찰 목록 조회 성공 (두 번째 페이지(page=1)이 조회됨)")
    void get_bids_by_auction_second_page_integrationtest() {
        // 두 번째 페이지, Size = 9 ( 조정 가능성이 큽니다.)
        int pageSize = 10;
        Pageable pageable = PageRequest.of(1, pageSize);

        // 1. 입찰 목록 조회
        PageResponse<BidResponse> result = bidService.getBidListForAuction(auctionId, pageable);

        // 2. 페이징 정보 검증
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(19);

        // 3. 입찰 목록의 개수가 동일한지 확인
        assertThat(result.getContent().size()).isLessThanOrEqualTo(pageSize);

        // 4. 정렬 순서 검증 (가격 내림차순 및 입찰 시간 오름차순)
        // data.sql 상의 가장 높은 입찰가는 191,000으로 1건
        // 최상위 입찰자는 입찰자 `J`입니다.
        Long memberIdJ = 11L;

        assertThat(result.getContent().getFirst().bidPrice()).isEqualTo(191000);
        assertThat(result.getContent().getFirst().memberId()).isEqualTo(memberIdJ);

        // `pageSize`가 10인 경우에 data.sql 상의 정보에 근거하여 10번째 입찰의 금액은 191,000입니다.
        assertThat(result.getContent().getLast().bidPrice()).isEqualTo(183000);
        // `pageSize`가 10인 경우에 data.sql 상의 정보에 근거하여 10번째 입찰자는 입찰자`S`입니다.
        // `pageSize`가 10인 경우에 입찰 내역의 마지막에 해당하는 유저
        Long memberIdS = 19L;
        assertThat(result.getContent().getLast().memberId()).isEqualTo(memberIdS);
    }

    @Test
    @DisplayName("경매 입찰 목록 조회 실패 (존재하지 않는 경매)")
    void get_bids_by_auction_fail_auction_not_found_integrationtest() {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(0, pageSize);

        // 1. AUCTION_NOT_FOUND `ErrorCode`에 대해 `Exception`이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            bidService.getBidListForAuction(auctionIdWrong, pageable);
        });

        // 2. 발생한 `Exception`이 `AUCTION_NOT_FOUND`인지 추가 검증
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.AUCTION_NOT_FOUND.getMessage());
        assertThat(exception.getStatus()).isEqualTo(ErrorCode.AUCTION_NOT_FOUND.getStatus());
    }

}
