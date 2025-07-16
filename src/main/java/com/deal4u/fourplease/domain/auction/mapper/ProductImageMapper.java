package com.deal4u.fourplease.domain.auction.mapper;

import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductImageMapper {

    public static ProductImage toEntity(Product product, String url) {
        return ProductImage.builder()
                .product(product)
                .url(url)
                .build();
    }

}
