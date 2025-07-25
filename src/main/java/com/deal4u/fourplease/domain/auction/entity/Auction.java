package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.common.BaseDateEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@Table(name = "auctions")
@SQLRestriction("deleted = false")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Product product;

    @Column(precision = 10, scale = 2)
    private BigDecimal startingPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal instantBidPrice;

    @Embedded
    private AuctionDuration duration;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    private boolean deleted;

    // 경매 종료
    public void close() {
        this.status = AuctionStatus.CLOSED;
    }

    // 패찰 상태로 변경
    public void fail() {
        this.status = AuctionStatus.FAIL;
    }

    // 삭제 처리
    public void delete() {
        this.deleted = true;
    }

    // 낙찰 상태로 변경
    public void markAsSuccess() {
        this.status = AuctionStatus.SUCCESS;
    }

    // 결제 완료 상태로 변경
    public void markPaymentCompleted() {
        this.status = AuctionStatus.PAYMENT_COMPLETED;
    }

    // 결제 실패 상태로 변경
    public void markPaymentFailed() {
        this.status = AuctionStatus.PAYMENT_FAILED;
    }

    // 배송 중 상태로 변경
    public void markShipping() {
        this.status = AuctionStatus.SHIPPING;
    }

    // 구매 확정 상태로 변경
    public void markPurchaseConfirmed() {
        this.status = AuctionStatus.PURCHASE_CONFIRMED;
    }
}
