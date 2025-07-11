package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.validator.Validator.validateListNotEmpty;

import com.deal4u.fourplease.domain.auction.dto.ProductImageListResponse;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import com.deal4u.fourplease.domain.auction.mapper.ProductImageMapper;
import com.deal4u.fourplease.domain.auction.repository.ProductImageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;

    @Transactional
    public void save(Product product, List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            ProductImage productImage = ProductImageMapper.toEntity(product, imageUrl);
            productImageRepository.save(productImage);
        }
    }

    @Transactional(readOnly = true)
    public ProductImageListResponse getByProduct(Product product) {
        List<ProductImage> productImageList = productImageRepository.findByProductId(
                product.getProductId());

        validateListNotEmpty(productImageList);

        return new ProductImageListResponse(productImageList);
    }
}
