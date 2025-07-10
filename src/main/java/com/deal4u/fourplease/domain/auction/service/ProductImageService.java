package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.ProductImageCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import com.deal4u.fourplease.domain.auction.repository.ProductImageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;

    public void save(Product product, List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            ProductImageCreateDto imgDto = new ProductImageCreateDto(product, imageUrl);
            ProductImage productImage = imgDto.toEntity();
            productImageRepository.save(productImage);
        }
    }
}
