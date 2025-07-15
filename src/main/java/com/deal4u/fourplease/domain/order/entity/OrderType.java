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

    public static OrderType fromString(String value) {

        validateIsNull(value);

        for (OrderType orderType : OrderType.values()) {
            if (orderType.getValue().equalsIgnoreCase(value)) {
                return orderType;
            }
        }
        throw new IllegalArgumentException();
    }

    private static void validateIsNull(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
    }
}
