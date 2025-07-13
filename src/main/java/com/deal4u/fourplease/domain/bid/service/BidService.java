package com.deal4u.fourplease.domain.bid.service;

import com.deal4u.fourplease.config.BidWebSocketHandler;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.BidMessageStatus;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.entity.PageResponse;
import com.deal4u.fourplease.domain.bid.mapper.BidMapper;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.Optional;
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

    @Transactional
    public void createBid(long memberId, BidRequest request) {
        // 1. Auction 조회
        Auction auction = auctionRepository.findById(request.auctionId())
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        // 2. 로그인 유저의 정보를 기반으로 Bidder 객체 생성
        Member member = memberRepository.findById(memberId)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);
        Bidder bidder = new Bidder(member);

        // 3. 기존 입찰 내역 조회
        Optional<Bid> existBidOptional = bidRepository.findByAuctionAndBidder(auction, bidder);

        if (existBidOptional.isPresent()) {
            Bid existBid = existBidOptional.get();
            // 3-1. 기존 입찰 금액보다 신규 입찰 금액이 큰 경우
            if (request.price() > existBid.getPrice().intValue()) {
                existBid.updatePrice(request.price());
            } else {
                throw ErrorCode.BID_FORBIDDEN_PRICE.toException();
            }
            bidWebSocketHandler.broadcastBid(existBid, BidMessageStatus.BID_UPDATED);
        } else {
            // 4. Bid Entity 객체 생성
            Bid bid = BidMapper.toEntity(auction, bidder, request.price());

            // 5. DB에 저장
            Bid save = bidRepository.save(bid);

            // 6. `WebSocket`의 모든 `Session`에 새 입찰 정보 전송
            bidWebSocketHandler.broadcastBid(save, BidMessageStatus.BID_CREATED);
        }
    }

    @Transactional
    public void deleteBid(long memberId, long bidId) {
        // 1. 로그인 유저의 정보를 기반으로 Bidder 객체 생성
        Member member = memberRepository.findById(memberId)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);
        Bidder bidder = new Bidder(member);

        // 2. 기존 입찰 내역 조회
        Bid existBid = bidRepository.findByBidIdAndBidder(bidId, bidder)
                .orElseThrow(ErrorCode.BID_NOT_FOUND::toException);

        // 3. 기존 일찰 취소
        existBid.delete();

        // 4. `WebSocket`의 모든 `Session`에 입찰 취소 정보 전송
        bidWebSocketHandler.broadcastBid(existBid, BidMessageStatus.BID_DELETED);
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
}
