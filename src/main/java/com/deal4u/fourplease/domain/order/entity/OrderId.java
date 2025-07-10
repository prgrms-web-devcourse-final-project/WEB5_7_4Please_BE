package com.deal4u.fourplease.domain.order.entity;

import com.deal4u.fourplease.domain.order.util.OrderIdGenerator;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderId {
    private String orderId;

    public static OrderId generate() {
        return new OrderId(OrderIdGenerator.generate());
    }
}
