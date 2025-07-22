package com.deal4u.fourplease.domain.review.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.review.entity.Review;
import com.deal4u.fourplease.domain.review.entity.Reviewer;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByAuctionAndReviewer(Auction auction, Reviewer reviewer);

    @Query("select r from Review r where r.seller = :seller ORDER BY  r.createdAt DESC")
    Page<Review> findBySeller(@Param("seller") Seller seller, Pageable pageable);
}
