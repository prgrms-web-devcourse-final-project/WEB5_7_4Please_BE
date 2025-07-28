package com.deal4u.fourplease.global.scheduler;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genMember;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionDuration;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.auction.service.AuctionStatusService;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import com.deal4u.fourplease.domain.settlement.service.HighestBidderNotifier;
import com.deal4u.fourplease.domain.settlement.service.SettlementService;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettlementScheduleServiceTest {

    @InjectMocks
    private SettlementService settlementService;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private HighestBidderNotifier highestBidderNotifier;

    @Mock
    private AuctionStatusService auctionStatusService;

    @Mock
    private SettlementScheduleService settlementScheduleService;

    @Test
    @DisplayName("정산 생성 시 결제 마감 스케줄이 정상적으로 등록.")
    void save_should_create_settlement_and_scheduleJob() {

        // Given
        Member member = genMember();
        Product product = genProduct();

        Long auctionId = 1L;
        int auctionDays = 3;
        Long bidId = 2L;
        int paymentDeadLineDays = 1;

        LocalDateTime auctionStartTime = LocalDateTime.now();
        LocalDateTime auctionEndTime = LocalDateTime.now().plusDays(auctionDays);

        // Auction 생성
        Auction auction = Auction.builder()
                .auctionId(auctionId)
                .product(product)
                .duration(new AuctionDuration(auctionStartTime, auctionEndTime))
                .status(AuctionStatus.CLOSE)
                .build();

        // Bid 생성
        Bid bid = Bid.builder()
                .bidId(bidId)
                .bidder(Bidder.createBidder(member))
                .isSuccessfulBidder(false).build();

        Long expectedSettlementId = 10L;
        LocalDateTime paymentDeadLine = auctionEndTime.plusDays(paymentDeadLineDays);

        Settlement settlement = Settlement.builder()
                .settlementId(expectedSettlementId)
                .auction(auction)
                .bidder(bid.getBidder())
                .paymentDeadline(paymentDeadLine)
                .build();

        // When
        when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusOpen(auctionId))
                .thenReturn(Optional.of(auction));
        when(bidRepository.findTopByAuctionOrderByPriceDescBidTimeAsc(auction))
                .thenReturn(Optional.of(bid));
        when(settlementRepository.save(any(Settlement.class)))
                .thenReturn(settlement);

        // when
        settlementService.save(auctionId, paymentDeadLineDays);

        // then
        // 1. 경매 종료
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.CLOSE);

        // 2. 낙찰자 설정
        assertThat(bid.isSuccessfulBidder()).isTrue();

        // 3. 정산 등록
        ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementRepository).save(settlementCaptor.capture());
        Settlement capturedSettlement = settlementCaptor.getValue();
        assertThat(capturedSettlement.getAuction().getAuctionId()).isEqualTo(auctionId);
        assertThat(capturedSettlement.getBidder()).isEqualTo(bid.getBidder());

        // 4. 스케줄러 호출 검증
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(settlementScheduleService, times(1))
                .scheduleSettlementClose(idCaptor.capture(), timeCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(expectedSettlementId);
        assertThat(timeCaptor.getValue()).isEqualTo(paymentDeadLine);
    }

    @Test
    @DisplayName("정산 생성 실패 (AuctionId가 존재하지 않는 경우)")
    void save_settlement_not_found_auction() {
        // Given
        Long notExistsAuctionId = 99L;
        int days = 1;

        // when
        when(auctionRepository.findByAuctionIdAndDeletedFalseAndStatusOpen(notExistsAuctionId))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> settlementService.save(notExistsAuctionId, days))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("해당 경매를 찾을 수 없습니다.");
    }

}
