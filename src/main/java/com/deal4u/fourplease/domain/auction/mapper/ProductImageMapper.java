package com.deal4u.fourplease.domain.auction.mapper;

import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import java.math.BigDecimal;
import java.util.List;

public class ProductImageMapper {

    public static ProductImage toEntity(Product product, String url) {
        return ProductImage.builder()
                .product(product)
                .url(url)
                .build();
    }

}
