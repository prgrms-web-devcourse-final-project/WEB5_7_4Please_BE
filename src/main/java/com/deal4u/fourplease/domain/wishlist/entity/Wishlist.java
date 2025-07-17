package com.deal4u.fourplease.domain.wishlist.entity;

import com.deal4u.fourplease.domain.common.BaseDateEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Wishlist extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;
    private Long memberId;
    private Long productId;

    private Boolean deleted = false;

}
