package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.ProductCreateRequest;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.CategoryRepository;
import com.deal4u.fourplease.domain.auction.repository.ProductRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
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

    public void save(ProductCreateRequest request, Member member) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        ErrorCode.ENTITY_NOT_FOUND.toException(
                                "id %d에 해당하는 카테고리가 없습니다.", request.categoryId()
                        )
                );
        Product product = request.toEntity(member, category);
        productImageService.save(product, request.imageUrls());

        productRepository.save(product);
    }

}
