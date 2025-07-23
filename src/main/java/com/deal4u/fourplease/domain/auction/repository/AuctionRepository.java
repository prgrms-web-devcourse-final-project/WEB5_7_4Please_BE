package com.deal4u.fourplease.domain.auction.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN FETCH a.product "
            + "WHERE a.auctionId = :auctionId")
    Optional<Auction> findByIdWithProduct(@Param("auctionId") Long auctionId);

    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.auctionId = :auctionId "
            + "AND a.deleted = false "
            + "AND a.status = 'OPEN'")
    Optional<Auction> findByAuctionIdAndDeletedFalseAndStatusOpen(
            @Param("auctionId") Long auctionId);

    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.auctionId = :auctionId "
            + "AND a.deleted = false "
            + "AND a.status = 'CLOSED'")
    Optional<Auction> findByAuctionIdAndDeletedFalseAndStatusClosed(
            @Param("auctionId") Long auctionId);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN FETCH a.product p "
            + "WHERE a.deleted = false "
            + "AND p.productId in :productIdList "
            + "ORDER BY a.createdAt DESC")
    Page<Auction> findAllByProductIdIn(
            @Param("productIdList") List<Long> productIdList,
            Pageable pageable
    );

    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.deleted = false")
    Page<Auction> findAll(Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product p "
            + "JOIN p.category c "
            + "WHERE a.deleted = false "
            + "AND p.name LIKE %:keyword% "
            + "AND c.categoryId = :categoryId")
    Page<Auction> findByKeywordAndCategoryId(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product p "
            + "WHERE a.deleted = false "
            + "AND p.name LIKE %:keyword%")
    Page<Auction> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product.category c "
            + "WHERE a.deleted = false "
            + "AND c.categoryId = :categoryId")
    Page<Auction> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "LEFT JOIN Bid b "
            + "ON b.auction = a "
            + "WHERE a.deleted = false "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC, "
            + "a.createdAt DESC")
    Page<Auction> findAllOrderByBidCount(Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product p "
            + "JOIN p.category c "
            + "LEFT JOIN Bid b "
            + "ON b.auction = a "
            + "WHERE a.deleted = false "
            + "AND p.name LIKE %:keyword% "
            + "AND c.categoryId = :categoryId "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC")
    Page<Auction> findByKeywordAndCategoryIdOrderByBidCount(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product p "
            + "LEFT JOIN Bid b "
            + "ON b.auction = a "
            + "WHERE a.deleted = false "
            + "AND p.name LIKE %:keyword% "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC")
    Page<Auction> findByKeywordOrderByBidCount(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product.category c "
            + "LEFT JOIN Bid b "
            + "ON b.auction = a "
            + "WHERE a.deleted = false "
            + "AND c.categoryId = :categoryId "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC")
    Page<Auction> findByCategoryIdOrderByBidCount(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}
