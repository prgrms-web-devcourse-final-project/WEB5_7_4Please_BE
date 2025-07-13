package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionId;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Product product;
    private Long startingPrice;
    private Long instantBidPrice;
    @Embedded
    private AuctionDuration duration;
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;
    private boolean deleted;

    public void close() {
        this.status = AuctionStatus.CLOSED;
    }
}
