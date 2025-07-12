package com.deal4u.fourplease.domain.auction.repository;

import com.deal4u.fourplease.domain.auction.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
