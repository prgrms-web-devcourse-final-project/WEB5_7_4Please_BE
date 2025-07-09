package com.deal4u.fourplease.domain.auction.entity;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class AuctionDuration {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
