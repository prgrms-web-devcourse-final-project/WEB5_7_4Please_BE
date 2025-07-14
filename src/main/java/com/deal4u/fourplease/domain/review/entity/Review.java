package com.deal4u.fourplease.domain.review.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.review.dto.ReviewRequest;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Embedded
    @AttributeOverride(name = "reviewer", column = @Column(name = "reviewer_member_id"))
    @AssociationOverride(name = "reviewer", joinColumns = @JoinColumn(name = "reviewer_member_id"))
    private Reviewer reviewer;

    @Embedded
    @AttributeOverride(name = "member", column = @Column(name = "seller_member_id"))
    @AssociationOverride(name = "member", joinColumns = @JoinColumn(name = "seller_member_id"))
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String content;

    private Review(Auction auction, Reviewer reviewer, Seller seller,
            Integer rating, String content) {
        this.auction = auction;
        this.reviewer = reviewer;
        this.seller = seller;
        this.content = content;
        this.rating = rating;
    }

    public static Review createReview(Auction auction, Reviewer reviewer, Seller seller,
            Integer rating, String content) {
        return new Review(auction, reviewer, seller, rating, content);
    }
}
