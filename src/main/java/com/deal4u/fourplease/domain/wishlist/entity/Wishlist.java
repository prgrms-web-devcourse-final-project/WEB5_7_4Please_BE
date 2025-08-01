package com.deal4u.fourplease.domain.wishlist.entity;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.common.BaseDateEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Wishlist extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    private Long memberId; // member

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    private boolean deleted;

    public void delete() {
        this.deleted = true;
    }

}
