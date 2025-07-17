package com.deal4u.fourplease.domain.bid.entity;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.common.BaseDateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@SQLRestriction("deleted = false")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;
    @Embedded
    private Bidder bidder;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    private LocalDateTime bidTime;
    private boolean isSuccessfulBidder;
    private boolean deleted;

    private Bid(Auction auction, Bidder bidder, int price) {
        this.auction = auction;
        this.bidder = bidder;
        this.price = BigDecimal.valueOf(price);
        this.bidTime = LocalDateTime.now();
        this.isSuccessfulBidder = false;
        this.deleted = false;
    }

    public static Bid createBid(Auction auction, Bidder bidder, int price) {
        return new Bid(auction, bidder, price);
    }

    public void update(boolean isSuccessfulBidder) {
        this.isSuccessfulBidder = isSuccessfulBidder;
    }

    public void delete() {
        this.deleted = true;
    }
}
