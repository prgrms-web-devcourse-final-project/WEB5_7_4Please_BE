package com.deal4u.fourplease.domain.order.util;

import java.util.UUID;

public class OrderIdGenerator {

    private OrderIdGenerator() {
        // 인스턴스화 방지
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
