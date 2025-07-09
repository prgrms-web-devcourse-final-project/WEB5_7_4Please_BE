package com.deal4u.fourplease.domain.bid.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;
    @ManyToOne(fetch = FetchType.LAZY)
    private Auction auction;
    @Embedded
    private Bidder bidder;
    private Long price;
    private LocalDateTime bidTime;
    private boolean isSuccessFulBidder;
    private boolean deleted;
}
