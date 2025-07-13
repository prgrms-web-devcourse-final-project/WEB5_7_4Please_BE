package com.deal4u.fourplease.domain.order.util;

import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_BID_PRICE;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_INSTANT_BID_PRICE;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_ORDER_TYPE;

import java.math.BigDecimal;

public class OrderValidator {

    private static final String BUY_NOW = "BUY_NOW";
    private static final String AWARD = "AWARD";

    private OrderValidator() {
    }

    public static void validateOrderType(String orderType) {
        if (!BUY_NOW.equals(orderType) && !AWARD.equals(orderType)) {
            throw INVALID_ORDER_TYPE.toException();
        }
    }

    public static void validateOrderPrice(BigDecimal requestPrice, BigDecimal expectedPrice,
                                          String orderType) {
        if (requestPrice.compareTo(expectedPrice) != 0) {
            throw (BUY_NOW.equals(orderType) ? INVALID_INSTANT_BID_PRICE :
                    INVALID_BID_PRICE).toException();
        }
    }
}
