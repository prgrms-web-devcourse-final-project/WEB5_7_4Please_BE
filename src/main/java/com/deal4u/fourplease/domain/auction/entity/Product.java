package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String name;
    private String description;
    private String thumbnailUrl;
    @Embedded
    private Address address;
    @Embedded
    private Seller seller;
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    private String phone;
}
