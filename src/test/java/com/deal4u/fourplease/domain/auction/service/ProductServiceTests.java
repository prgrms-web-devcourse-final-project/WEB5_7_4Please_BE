package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genMember;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genProduct;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genProductCreateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("상품을 등록하고 등록된 상품을 반환한다")
    void product_can_be_saved_and_returned() throws Exception {

        ProductCreateDto req = genProductCreateDto();
        Member seller = genMember();

        Category category = new Category(4L, "생활용품");
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(category));

        Product actualProduct = productService.save(req);

        verify(productImageService).save(any(Product.class), eq(req.imageUrls()));

        assertThat(req.name()).isEqualTo(actualProduct.getName());
        assertThat(req.description()).isEqualTo(actualProduct.getDescription());
        assertThat(req.address()).isEqualTo(actualProduct.getAddress().address());
        assertThat(req.addressDetail()).isEqualTo(actualProduct.getAddress().addressDetail());
        assertThat(req.zipCode()).isEqualTo(actualProduct.getAddress().zipCode());
        assertThat(req.phone()).isEqualTo(actualProduct.getPhone());
        assertThat(category).isEqualTo(actualProduct.getCategory());
        assertThat(seller.getEmail()).isEqualTo(actualProduct.getSeller().getMember().getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 id로 카테고리 조회 시 예외가 발생한다")
    void throws_if_category_not_found() throws Exception {

        ProductCreateDto wrongRequest = genProductCreateDto();

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> {
                    productService.save(wrongRequest);
                }
        ).isInstanceOf(GlobalException.class);

    }

    @Test
    @DisplayName("product를 인자로 받아 soft delete를 실행한다")
    void delete_product_should_soft_delete_product_by_product() throws Exception {

        Product product = genProduct();

        productService.deleteProduct(product);

        verify(productImageService).deleteProductImage(product);

        assertThat(product.isDeleted()).isTrue();

    }

}