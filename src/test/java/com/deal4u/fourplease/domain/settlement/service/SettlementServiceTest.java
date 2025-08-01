package com.deal4u.fourplease.domain.settlement.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.service.AuctionStatusService;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.scheduler.FailedSettlementScheduleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private FailedSettlementScheduleService scheduleService;

    @Mock
    private SecondBidderNotifier secondBidderNotifier;

    @Mock
    private SettlementStatusService settlementStatusService;

    @Mock
    private AuctionStatusService auctionStatusService;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    @DisplayName("정산 상태를 성공으로 변경한다")
    void changeSettlementSuccess() {
        // given
        Auction auction = createAuction();
        Settlement settlement = createSettlement(auction);
        final LocalDateTime paidAt = LocalDateTime.now();

        given(settlementRepository.findPendingSettlementByAuction(auction,
                SettlementStatus.PENDING))
                .willReturn(Optional.of(settlement));

        Mockito.doAnswer(input -> {
                    Settlement inputSettelment = input.getArgument(0);
                    inputSettelment.updateStatus(SettlementStatus.SUCCESS, LocalDateTime.now(), null);
                    return null;
                }).when(settlementStatusService)
                .markSettlementAsSuccess(settlement);
        Mockito.doAnswer(input -> {
            Auction argument = input.getArgument(0);
            argument.markAsSuccess();
            return null;
        }).when(auctionStatusService).markAuctionAsSuccess(auction);
        // when
        settlementService.changeSettlementSuccess(auction);

        // then
        then(settlementRepository).should()
                .findPendingSettlementByAuction(auction, SettlementStatus.PENDING);
        then(scheduleService).should().cancelFailedSettlement(settlement.getSettlementId());
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.SUCCESS);
        assertThat(settlement.getPaidAt()).isNotNull();
        assertThat(settlement.getPaidAt()).isAfter(paidAt.minusSeconds(1));
    }

    @Test
    @DisplayName("정산을 찾을 수 없으면 예외를 발생시킨다")
    void changeSettlementSuccessSettlementNotFound() {
        // given
        Auction auction = createAuction();

        given(settlementRepository.findPendingSettlementByAuction(auction,
                SettlementStatus.PENDING))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> settlementService.changeSettlementSuccess(auction))
                .isInstanceOf(GlobalException.class)
                .hasMessage("해당 정산을 찾을 수 없습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("차상위 입찰자에게 정산을 제안한다")
    void offerSecondBidder() {
        // given
        Long auctionId = 1L;
        Auction auction = createAuction();
        Bidder bidder = createBidder();
        Bid highestBid = createBid(auction, createBidder());
        Bid secondHighestBid = createBid(auction, bidder);
        Settlement savedSettlement = createSettlement(auction);

        given(bidRepository.findTop2ByAuctionId(auctionId))
                .willReturn(List.of(highestBid, secondHighestBid));
        given(settlementRepository.existsByAuctionAndBidder(auction, bidder))
                .willReturn(false);
        given(settlementRepository.save(any(Settlement.class)))
                .willReturn(savedSettlement);

        // when
        settlementService.offerSecondBidder(auctionId);

        // then
        ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementRepository).save(settlementCaptor.capture());

        then(secondBidderNotifier).should().send(
                eq(secondHighestBid),
                eq(auction),
                any(LocalDateTime.class)
        );
        Settlement capturedSettlement = settlementCaptor.getValue();
        assertAll(
                () -> assertThat(capturedSettlement.getAuction()).isEqualTo(auction),
                () -> assertThat(capturedSettlement.getBidder()).isEqualTo(bidder),
                () -> assertThat(capturedSettlement.getStatus()).isEqualTo(
                        SettlementStatus.PENDING),
                () -> assertThat(capturedSettlement.getPaymentDeadline()).isAfter(
                        LocalDateTime.now().plusHours(47))
        );

        then(scheduleService).should().scheduleFailedSettlement(
                eq(savedSettlement.getSettlementId()),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("차상위 입찰자를 찾을 수 없으면 예외를 발생시킨다")
    void offerSecondBidderSecondHighestBidderNotFound() {
        // given
        Long auctionId = 1L;

        Bid onlyBid = createBid(createAuction(), createBidder());

        given(bidRepository.findTop2ByAuctionId(auctionId))
                .willReturn(List.of(onlyBid)); // 입찰이 1개만 있는 경우

        // when & then
        assertThatThrownBy(() -> settlementService.offerSecondBidder(auctionId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("차상위 입찰자를 찾을 수 없습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("이미 정산이 존재하면 예외를 발생시킨다")
    void offerSecondBidderSettlementAlreadyExists() {
        // given
        Long auctionId = 1L;
        Auction auction = createAuction();
        Bidder bidder = createBidder();

        Bid highestBid = createBid(auction, createBidder());
        Bid secondHighestBid = createBid(auction, bidder);

        given(bidRepository.findTop2ByAuctionId(auctionId))
                .willReturn(List.of(highestBid, secondHighestBid));
        given(settlementRepository.existsByAuctionAndBidder(auction, bidder))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> settlementService.offerSecondBidder(auctionId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("이미 차상위 입찰자에 대한 정산이 존재합니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("실패한 정산을 처리한다")
    void handleFailedSettlement() {
        // given
        Long settlementId = 1L;
        Auction auction = createAuction();
        Settlement settlement = createSettlement(auction);

        given(settlementRepository.findById(settlementId))
                .willReturn(Optional.of(settlement));
        Mockito.doAnswer(input -> {
                    Settlement inputSettelment = input.getArgument(0);
                    String rejectedReason = input.getArgument(1);
                    inputSettelment.updateStatus(SettlementStatus.REJECTED, null, rejectedReason);
                    return null;
                }).when(settlementStatusService)
                .markSettlementAsRejected(eq(settlement), anyString());
        Mockito.doAnswer(input -> {
            Auction argument = input.getArgument(0);
            argument.fail();
            return null;
        }).when(auctionStatusService).failAuction(auction);

        // when
        settlementService.handleFailedSettlement(settlementId);

        // then
        then(settlementRepository).should().findById(settlementId);

        assertAll(
                () -> assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.REJECTED),
                () -> assertThat(settlement.getRejectedReason()).isEqualTo(
                        "결제 기한 내에 결제를 완료하지 않았습니다.")
        );
    }

    @Test
    @DisplayName("실패한 정산 처리 시 정산을 찾을 수 없으면 예외를 발생시킨다")
    void handleFailedSettlementSettlementNotFound() {
        // given
        Long settlementId = 1L;

        given(settlementRepository.findById(settlementId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> settlementService.handleFailedSettlement(settlementId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("해당 정산을 찾을 수 없습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Auction createAuction() {
        return Auction.builder()
                .auctionId(1L)
                .build();
    }

    private Settlement createSettlement(Auction auction) {
        return Settlement.builder()
                .settlementId(1L)
                .auction(auction)
                .bidder(createBidder())
                .status(SettlementStatus.PENDING)
                .paymentDeadline(LocalDateTime.now().plusHours(48))
                .build();
    }

    private Bidder createBidder() {
        Member member = createMember();
        return Bidder.createBidder(member);
    }

    private Member createMember() {
        return Member.builder()
                .memberId(1L)
                .build();
    }

    private Bid createBid(Auction auction, Bidder bidder) {
        return Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .build();
    }
}
