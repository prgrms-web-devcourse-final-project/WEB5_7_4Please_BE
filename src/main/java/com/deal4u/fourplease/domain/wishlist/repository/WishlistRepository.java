package com.deal4u.fourplease.domain.wishlist.repository;

import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

}
