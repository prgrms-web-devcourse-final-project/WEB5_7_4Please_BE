package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.Category;

public record CategoryDto(
        Long categoryId,
        String name
) {
    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getCategoryId(),
                category.getName()
        );
    }
}
