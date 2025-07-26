package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import java.util.ArrayList;
import java.util.List;

public record ProductImageListResponse(List<ProductImage> productImages) {

    public List<String> toProductImageUrls() {
        List<String> imageUrls = new ArrayList<>();

        for (ProductImage image : productImages) {
            imageUrls.add(image.getUrl());
        }

        return imageUrls;
    }
}
