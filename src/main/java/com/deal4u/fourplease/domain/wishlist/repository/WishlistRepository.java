package com.deal4u.fourplease.domain.wishlist.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w "
            + "FROM Wishlist w "
            + "WHERE w.deleted = false "
            + "AND w.memberId = :memberId")
    Page<Wishlist> findAll(Pageable pageable, @Param("memberId") Long memberId);

    @Query("SELECT w.auction.auctionId "
            + "FROM Wishlist w "
            + "WHERE w.auction.auctionId IN :auctionIds "
            + "AND w.memberId = :memberId "
            + "AND w.deleted = false")
    List<Long> findAuctionIdsInWishlist(
            @Param("auctionIds") List<Long> auctionIds,
            @Param("memberId") Long memberId
    );

    @Query("SELECT w "
            + "FROM Wishlist w "
            + "WHERE w.deleted = false "
            + "AND w.auction = :auction "
            + "AND w.memberId = :memberId")
    Optional<Wishlist> findWishlist(@Param("auction") Auction auction,
            @Param("memberId") Long memberId);
}