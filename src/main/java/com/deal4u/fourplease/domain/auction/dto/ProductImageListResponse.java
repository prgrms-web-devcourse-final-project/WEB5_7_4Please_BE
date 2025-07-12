package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import java.util.ArrayList;
import java.util.List;

public record ProductImageListResponse(List<ProductImage> productImageList) {
    public List<String> toProductImageUrlList() {
        List<String> imageUrls = new ArrayList<>();

        for (ProductImage image : productImageList) {
            imageUrls.add(image.getUrl());
        }

        return imageUrls;
    }
}
