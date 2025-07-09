package com.deal4u.fourplease.domain.order.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Auction;
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
import org.hibernate.annotations.NaturalId;

@Entity
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
    private BigDecimal amount;
    @Embedded
    private Address address;
    private String phone;
    private String content;
    private String receiver;
}
