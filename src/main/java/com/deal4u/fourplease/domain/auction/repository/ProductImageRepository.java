package com.deal4u.fourplease.domain.auction.repository;

import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("select pi from ProductImage pi where pi.product.productId = :productId")
    List<ProductImage> findByProductId(@Param("productId") Long productId);

}
