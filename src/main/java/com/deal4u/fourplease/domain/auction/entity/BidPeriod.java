package com.deal4u.fourplease.domain.auction.entity;


import com.deal4u.fourplease.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDateTime;

public enum BidPeriod {
    THREEDAY(3, "3Day"),
    FIVEDAY(5, "5Day");

    private final Integer duration;
    private final String label;

    BidPeriod(Integer duration, String label) {
        this.duration = duration;
        this.label = label;
    }

    public LocalDateTime getEndTime(LocalDateTime startTime) {
        return startTime.plusDays(duration);
    }

    @JsonCreator
    public static BidPeriod of(String label) {
        for (BidPeriod bidPeriod : values()) {
            if (bidPeriod.label.equals(label)) {
                return bidPeriod;
            }
        }
        throw ErrorCode.BIDPERIOD_NOT_FOUND.toException(label);
    }
}