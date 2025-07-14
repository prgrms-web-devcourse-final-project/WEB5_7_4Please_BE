package com.deal4u.fourplease.domain.review.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.review.dto.ReviewRequest;
import com.deal4u.fourplease.domain.review.entity.Review;
import com.deal4u.fourplease.domain.review.entity.Reviewer;
import com.deal4u.fourplease.domain.review.mapper.ReviewMapper;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;

    public void createReview(ReviewRequest request, Long memberId) {
        // 1. Auction 조회
        Auction auction = auctionRepository.findById(request.auctionId())
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        // 2. 로그인 유저의 정보를 기반으로 Bidder 객체 생성
        Bidder bidder = getBidder(memberId);

        // 3. 기존 리뷰 존재 유무 확인
        Optional<Review> existReviewOptional = reviewRepository.findByAuctionAndBidder(auction,
                bidder);

        // 3-1. 기존 리뷰가 존재하는 경우 예외 처리
        if (existReviewOptional.isPresent()) {
            throw ErrorCode.REVIEW_ALREADY_EXISTS.toException();
        }

        // 4. 기존 입찰 내역 조회
        Optional<Bid> existBidOptional = bidRepository
                .findTopByAuctionAndBidderOrderByPriceDesc(auction, bidder);

        // 4. 기존 입찰 내여 존재 유무 확인
        if (existBidOptional.isPresent()) {
            Bid bid = existBidOptional.get();
            // 4-1. 낙찰된 입찰인지 확인
            if (bid.isSuccessfulBidder()) {
                // Reviewer 생성
                Reviewer reviewer = Reviewer.createReviewer(bidder.getMember());

                // Review 생성
                Review review = ReviewMapper.toEntity(auction, reviewer, auction.getProduct()
                        .getSeller(), request.rating(), request.content());
                // Review 등록
                reviewRepository.save(review);
            } else {
                // 4-2. 낙찰된 입찰이 아닌 경우 예외 처리
                throw ErrorCode.INVALID_AUCTION_BIDDER.toException();
            }
        } else {
            // 5. 입찰 내역이 없는 경우 예외 처리
            throw ErrorCode.BID_NOT_FOUND.toException();
        }

    }

    // getBidder 부분은 `util`로 분리할지 검토중입니다.
    private Bidder getBidder(long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);
        return Bidder.createBidder(member);
    }
}
