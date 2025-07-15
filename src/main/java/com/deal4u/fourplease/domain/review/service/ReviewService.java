package com.deal4u.fourplease.domain.review.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.ORDER_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_NOT_SUCCESS;
import static com.deal4u.fourplease.global.exception.ErrorCode.USER_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderStatus;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.payment.repository.PaymentRepository;
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
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public void createReview(ReviewRequest request, Long memberId) {
        // 1. 경매 검증
        Auction auction = getAuctionOrThrow(request.auctionId());

        // 2. 로그인 유저의 정보를 기반으로 주문 및 결제 검증
        Member member = validatePaidOrderAndGetMember(memberId, auction);

        // 3. 기존 리뷰 유무를 확인
        Reviewer reviewer = getReviewerOrThrowIfExists(member, auction);

        // 4. 리뷰 작성
        saveReview(request, auction, reviewer);
    }

    private void saveReview(ReviewRequest request, Auction auction, Reviewer reviewer) {
        Review review = ReviewMapper.toEntity(auction, reviewer, auction.getProduct()
                .getSeller(), request.rating(), request.content());
        reviewRepository.save(review);
    }

    private Reviewer getReviewerOrThrowIfExists(Member member, Auction auction) {
        Reviewer reviewer = Reviewer.createReviewer(member);
        validateReviewAbsence(auction, reviewer);
        return reviewer;
    }

    private void validateReviewAbsence(Auction auction, Reviewer reviewer) {
        Optional<Review> existReviewOptional = reviewRepository
                .findByAuctionAndReviewer(auction, reviewer);

        if (existReviewOptional.isPresent()) {
            throw ErrorCode.REVIEW_ALREADY_EXISTS.toException();
        }
    }

    private Member validatePaidOrderAndGetMember(Long memberId, Auction auction) {
        Member member = getMemberOrThrow(memberId);
        Orderer orderer = Orderer.createOrderer(member);
        Order order = getOrderOrThrow(orderer, auction);
        getPaymentOrThrow(order);
        return member;
    }

    // 以下 검증 로직 (他 서비스에서도 이용하기 때문에, Util Class 등으로 추출 검토)
    private Auction getAuctionOrThrow(Long auctionId) {
        return auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(USER_NOT_FOUND::toException);
    }

    private Order getOrderOrThrow(Orderer orderer, Auction auction) {
        return orderRepository.findByOrdererAndAuctionAndStatus(orderer, auction,
                        OrderStatus.SUCCESS)
                .orElseThrow(ORDER_NOT_FOUND::toException);
    }

    private void getPaymentOrThrow(Order order) {
        paymentRepository.findByOrderId(order.getOrderId())
                .orElseThrow(PAYMENT_NOT_SUCCESS::toException);
    }
}
