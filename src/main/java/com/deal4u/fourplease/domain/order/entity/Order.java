package com.deal4u.fourplease.domain.order.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.order.dto.OrderUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @NaturalId
    private OrderId orderId;
    @Embedded
    private Orderer orderer;
    @ManyToOne(fetch = FetchType.LAZY)
    private Auction auction;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    @Embedded
    private Address address;
    private String phone;
    private String content;
    private String receiver;

    public static Order createOrder(Auction auction, Orderer orderer, OrderId orderId,
                                    BigDecimal orderPrice) {
        return Order.builder()
                .orderId(orderId)
                .auction(auction)
                .orderer(orderer)
                .price(orderPrice)
                .build();
    }

    public void updateOrder(OrderUpdateRequest orderUpdateRequest) {
        this.address = new Address(orderUpdateRequest.address(), orderUpdateRequest.addressDetail(),
                orderUpdateRequest.zipCode());
        this.phone = orderUpdateRequest.phone();
        this.content = orderUpdateRequest.content();
        this.receiver = orderUpdateRequest.receiver();
    }
}
