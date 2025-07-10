package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.Product;

import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductImageCreateDto(
        @NotNull
        Product product,
        @NotBlank
        String url
) {
    public ProductImage toEntity() {
        return ProductImage.builder()
                .product(product)
                .url(url)
                .build();
    }
}
