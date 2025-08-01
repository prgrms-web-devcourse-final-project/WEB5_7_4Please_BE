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

    @Query("select r from Review r where r.seller = :seller")
    Page<Review> findBySeller(@Param("seller") Seller seller, Pageable pageable);

    // 판매자별 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.seller.member.memberId = :sellerId")
    Integer countBySellerMemberId(@Param("sellerId") Long sellerId);

    // 판매자별 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.member.memberId = :sellerId")
    Double getAverageRatingBySellerMemberId(@Param("sellerId") Long sellerId);
}
