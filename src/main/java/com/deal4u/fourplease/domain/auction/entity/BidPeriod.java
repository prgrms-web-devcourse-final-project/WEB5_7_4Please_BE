package com.deal4u.fourplease.domain.auction.entity;


import java.time.LocalDateTime;

public enum BidPeriod {
    THREE(3),
    FIVE(5),
    SEVEN(7),;

    private final Integer duration;

    BidPeriod(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime(LocalDateTime startTime) {
        return startTime.plusDays(duration);
    }

}