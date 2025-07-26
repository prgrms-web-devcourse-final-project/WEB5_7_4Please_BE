package com.deal4u.fourplease.domain.bid.service;

import com.deal4u.fourplease.config.BidWebSocketHandler;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.BidMessageStatus;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.mapper.BidMapper;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final BidWebSocketHandler bidWebSocketHandler;
    private final NamedLockProvider namedLockProvider;

    @Transactional
    public void createBid(long memberId, BidRequest request) {
        // 1. 경매 ID를 기반으로 Lock 키 생성
        String lockKey = "auction-lock:" + request.auctionId();
        NamedLock lock = namedLockProvider.getPassLock(lockKey);

        try {
            // 1. Lock 처리
            lock.lock();

            // 2. Auction 조회
            Auction auction = auctionRepository.findById(request.auctionId())
                    .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

            // 3. Lock 처리 이후에 경매 상태 확인
            if (auction.getStatus() != AuctionStatus.OPEN) {
                throw ErrorCode.AUCTION_NOT_OPEN.toException();
            }

            // 4. 로그인 유저의 정보를 기반으로 Bidder 객체 생성
            Bidder bidder = getBidder(memberId);

            // 5. 기존 입찰 내역 조회
            Optional<Bid> existBidOptional = bidRepository
                    .findTopByAuctionAndBidderOrderByPriceDesc(auction, bidder);

            if (existBidOptional.isPresent()) {
                Bid existBid = existBidOptional.get();
                // 5-1. 기존 입찰 금액보다 신규 입찰 금액이 큰 경우
                if (request.price() > existBid.getPrice().intValue()) {
                    createBid(request, auction, bidder);
                } else {
                    throw ErrorCode.BID_FORBIDDEN_PRICE.toException();
                }
            } else {
                // 5-2. 신규 입찰
                createBid(request, auction, bidder);
            }
        } finally {
            lock.unlock();
        }

    }

    private void createBid(BidRequest request, Auction auction, Bidder bidder) {
        // 1. Bid Entity 객체 생성
        Bid bid = BidMapper.toEntity(auction, bidder, request.price());

        // 2. DB에 저장
        Bid save = bidRepository.save(bid);

        // 3. `WebSocket`의 모든 `Session`에 새 입찰 정보 전송
        bidWebSocketHandler.broadcastBid(save, BidMessageStatus.BID_CREATED);
    }

    @Transactional
    public void deleteBid(long memberId, long bidId) {
        // 1. 로그인 유저의 정보를 기반으로 Bidder 객체 생성
        Bidder bidder = getBidder(memberId);

        // 2. 기존 입찰 내역 조회
        Bid existBid = bidRepository.findByBidIdAndBidder(bidId, bidder)
                .orElseThrow(ErrorCode.BID_NOT_FOUND::toException);

        String lockKey = "auction-lock:" + existBid.getAuction().getAuctionId();
        NamedLock lock = namedLockProvider.getPassLock(lockKey);

        try {
            // 3. Lock 처리
            lock.lock();

            // 4. Auction 조회
            Auction auction = auctionRepository.findById(existBid.getAuction().getAuctionId())
                    .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

            // 5. Lock 처리 이후에 경매 상태 확인
            if (auction.getStatus() != AuctionStatus.OPEN) {
                throw ErrorCode.AUCTION_NOT_OPEN.toException();
            }

            // 6. 기존 입찰 취소
            existBid.delete();

            // 7. `WebSocket`의 모든 `Session`에 입찰 취소 정보 전송
            bidWebSocketHandler.broadcastBid(existBid, BidMessageStatus.BID_DELETED);

        } finally {
            lock.unlock();
        }
    }

    // bid max price와 bidCount를 반환
    @Transactional(readOnly = true)
    public BidSummaryDto getBidSummaryDto(Long auctionId) {
        List<BigDecimal> bidList = bidRepository.findPricesByAuctionIdOrderByPriceDesc(
                auctionId
        );
        return BidSummaryDto.toBidSummaryDto(bidList);
    }

    public PageResponse<BidResponse> getBidListForAuction(Long auctionId, Pageable pageable) {
        // 1. Auction 조회
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        // 2. 경매 ID를 기반으로 입찰 내역 조회
        Page<Bid> bidPage = bidRepository.findByAuctionAndDeletedFalseOrderByPriceDescBidTimeAsc(
                auction,
                pageable);

        // 3. `BidResponse`로 `Mapper`를 이용해서 Mapping
        Page<BidResponse> bidResponsePage = bidPage.map(BidMapper::toResponse);
        return PageResponse.fromPage(bidResponsePage);
    }


    private Bidder getBidder(long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);
        return Bidder.createBidder(member);
    }

    public Map<Long, BidSummaryDto> getBidSummaryDtoMap(List<Long> auctionIds) {
        // IN으로 한번에 가져오기
        Map<Long, List<BigDecimal>> auctionBidPricesMap =
                bidRepository.findPricesByAuctionIdsGrouped(auctionIds);

        // BudSummaryDto로 변환
        Map<Long, BidSummaryDto> bidSummaryDtoMap = new HashMap<>();
        for (Map.Entry<Long, List<BigDecimal>> entry : auctionBidPricesMap.entrySet()) {
            Long auctionId = entry.getKey();
            List<BigDecimal> bidPrices = entry.getValue();

            BidSummaryDto bidSummaryDto = BidSummaryDto.toBidSummaryDto(bidPrices);
            bidSummaryDtoMap.put(auctionId, bidSummaryDto);
        }

        return bidSummaryDtoMap;
    }
}
