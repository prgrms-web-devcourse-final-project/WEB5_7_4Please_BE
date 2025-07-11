package com.deal4u.fourplease.domain.bid.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction("deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;
    @ManyToOne(fetch = FetchType.LAZY)
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
        this.price = new BigDecimal(price);
        this.bidTime = LocalDateTime.now();
        this.isSuccessfulBidder = false;
        this.deleted = false;
    }

    public static Bid createBid(Auction auction, Bidder bidder, int price) {
        return new Bid(auction, bidder, price);
    }

    public void updatePrice(int price) {
        this.price = new BigDecimal(price);
    }

    public void delete() {
        this.deleted = true;
    }
}
