package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.service.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.CategoryRepository;
import com.deal4u.fourplease.domain.auction.repository.ProductRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageService productImageService;

    @Test
    @DisplayName("상품을 등록할 수 있다")
    void product_can_be_saved() throws Exception {

        ProductCreateDto req = genProductCreateDto();
        Member seller = genMember();

        Category category = new Category(4L, "생활용품");
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(category));

        productService.save(req, seller);

        verify(productImageService).save(any(Product.class), eq(req.imageUrls()));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(req.name()).isEqualTo(savedProduct.getName());
        assertThat(req.description()).isEqualTo(savedProduct.getDescription());
        assertThat(req.address()).isEqualTo(savedProduct.getAddress().address());
        assertThat(req.addressDetail()).isEqualTo(savedProduct.getAddress().addressDetail());
        assertThat(req.zipCode()).isEqualTo(savedProduct.getAddress().zipCode());
        assertThat(req.phone()).isEqualTo(savedProduct.getPhone());
        assertThat(category).isEqualTo(savedProduct.getCategory());
        assertThat(seller).isEqualTo(savedProduct.getSeller().getMember());
    }

    @Test
    @DisplayName("존재하지 않는 id로 카테고리 조회 시 예외가 발생한다")
    void throws_if_category_not_found() throws Exception {

        ProductCreateDto wrongRequest = genProductCreateDto();
        Member seller = mock(Member.class);

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> {
                    productService.save(wrongRequest, seller);
                }
        ).isInstanceOf(GlobalException.class);
    
    }

}