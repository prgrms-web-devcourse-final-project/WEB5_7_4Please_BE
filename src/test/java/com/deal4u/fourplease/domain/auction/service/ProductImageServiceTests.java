package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.service.TestUtils.*;
import static com.deal4u.fourplease.domain.auction.validator.Validator.validateListNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.ProductImageListResponse;
import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import com.deal4u.fourplease.domain.auction.repository.ProductImageRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTests {

    @InjectMocks
    private ProductImageService productImageService;

    @Mock
    private ProductImageRepository productImageRepository;

    @Test
    @DisplayName("상품 이미지 url 리스트를 받아 등록할 있다")
    void product_image_can_be_saved() throws Exception {

        Product product = mock(Product.class);
        List<String> imageUrls = List.of(
                "http://example.com/image1.jpg",
                "http://example.com/image2.jpg"
        );

        productImageService.save(product, imageUrls);

        ArgumentCaptor<ProductImage> imageCaptor = ArgumentCaptor.forClass(ProductImage.class);

        verify(productImageRepository, times(2)).save(imageCaptor.capture());
        List<ProductImage> savedImages = imageCaptor.getAllValues();

        assertThat(savedImages).hasSize(2);
        assertThat(savedImages.get(0).getProduct()).isEqualTo(product);
        assertThat(savedImages.get(0).getUrl()).isEqualTo(imageUrls.get(0));
        assertThat(savedImages.get(1).getProduct()).isEqualTo(product);
        assertThat(savedImages.get(1).getUrl()).isEqualTo(imageUrls.get(1));

    }

    @Test
    @DisplayName("product를 인자로 받아 productId로 이미지 리스트를 찾아 반환한다")
    void return_product_image_list_by_product_id() throws Exception {

        Product product = genProduct();
        List<ProductImage> productImageList = List.of(
                new ProductImage(1L, product, "http://example.com/image1.jpg"),
                new ProductImage(2L, product, "http://example.com/image2.jpg")
        );

        when(productImageRepository.findByProductId(product.getProductId()))
                .thenReturn(productImageList);


        ProductImageListResponse actualResp = productImageService.getByProduct(product);
        List<String> actualImageUrls = actualResp.toProductImageUrlList();

        assertThat(actualImageUrls).containsExactly(
                "http://example.com/image1.jpg",
                "http://example.com/image2.jpg"
        );

    }


}