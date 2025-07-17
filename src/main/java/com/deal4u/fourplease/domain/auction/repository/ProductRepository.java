package com.deal4u.fourplease.domain.auction.repository;

import com.deal4u.fourplease.domain.auction.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p "
            + "where p.seller.member.memberId = :sellerId "
            + "and p.deleted = false")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);
}
