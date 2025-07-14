package com.deal4u.fourplease.domain.auction.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    private Long categoryId;
    private String name;
}
