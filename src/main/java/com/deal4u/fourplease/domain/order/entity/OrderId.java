package com.deal4u.fourplease.domain.order.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class OrderId {

    private String orderId;
}
