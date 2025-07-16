package com.deal4u.fourplease.domain.review.mapper;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.review.dto.ReviewRequest;
import com.deal4u.fourplease.domain.review.dto.ReviewResponse;
import com.deal4u.fourplease.domain.review.entity.Review;
import com.deal4u.fourplease.domain.review.entity.Reviewer;

public class ReviewMapper {

    private ReviewMapper() {
    }

    public static Review toEntity(Auction auction, Reviewer reviewer, Seller seller, Integer rating,
            String content) {
        return Review.createReview(auction, reviewer, seller, rating, content);
    }

    public static ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getReviewId(),
                review.getReviewer().getReviewer().getNickName(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }

}
