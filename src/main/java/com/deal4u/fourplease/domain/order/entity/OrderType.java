package com.deal4u.fourplease.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderType {
    BUY_NOW("BUY_NOW"),
    AWARD("AWARD");

    private final String value;

    OrderType(String value) {
        this.value = value;
    }
}
