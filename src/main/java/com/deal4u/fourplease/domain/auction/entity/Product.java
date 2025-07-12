package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@Table(name = "products")
@SQLRestriction("deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String name;
    private String description;

    @Column(nullable = false)
    private String thumbnailUrl;

    @Embedded
    private Address address;

    @Embedded
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    private String phone;

    private boolean deleted;

    public void softDelete() {
        this.deleted = true;
    }
}
