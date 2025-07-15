package com.deal4u.fourplease.domain.review.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.review.entity.Review;
import com.deal4u.fourplease.domain.review.entity.Reviewer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByAuctionAndReviewer(Auction auction, Reviewer reviewer);
}
