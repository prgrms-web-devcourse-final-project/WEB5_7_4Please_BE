package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.validator.Validator.validateListNotEmpty;

import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.CategoryRepository;
import com.deal4u.fourplease.domain.auction.repository.ProductRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageService productImageService;

    @Transactional
    public Product save(ProductCreateDto request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(ErrorCode.CATEGORY_NOT_FOUND::toException);
        Product product = request.toEntity(category);
        productImageService.save(product, request.imageUrls());

        productRepository.save(product);
        return product;
    }

    @Transactional
    public void deleteProduct(Product targetProduct) {
        productImageService.deleteProductImage(targetProduct);
        targetProduct.delete();
    }

    public List<Product> getProductListBySellerId(Long sellerId) {
        List<Product> productList = productRepository.findBySellerId(sellerId);
        validateListNotEmpty(productList);
        return productList;
    }
}
