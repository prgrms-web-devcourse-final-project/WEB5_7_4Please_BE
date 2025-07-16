package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @AttributeOverrides(value = {
            @AttributeOverride(name = "address", column = @Column(name = "address")),
            @AttributeOverride(name = "addressDetail", column = @Column(name = "detail_address")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "zip_code"))
    })
    private Address address;

    @Embedded
    @AttributeOverride(name = "member", column = @Column(name = "seller_member_id"))
    @AssociationOverride(name = "member", joinColumns = @JoinColumn(name = "seller_member_id"))
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    private String phone;

    private boolean deleted;

    public void delete() {
        this.deleted = true;
    }
}
