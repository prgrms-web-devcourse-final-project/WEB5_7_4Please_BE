package com.deal4u.fourplease.domain.auction.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
            + "JOIN FETCH a.product p "
            + "JOIN FETCH p.seller s "
            + "JOIN FETCH s.member m "
            + "WHERE a.auctionId = :auctionId")
    Optional<Auction> findByIdWithProductAndSellerAndMember(@Param("auctionId") Long auctionId);

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
            + "AND a.status = 'CLOSE'")
    Optional<Auction> findByAuctionIdAndDeletedFalseAndStatusClosed(
            @Param("auctionId") Long auctionId);

    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.deleted = false "
            + "AND a.product.seller.member.memberId = :sellerId "
            + "ORDER BY a.createdAt DESC")
    @EntityGraph(attributePaths = {"product"})
    Page<Auction> findAllBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.deleted = false "
            + "AND a.status = 'OPEN'")
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Auction> findAll(Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product p "
            + "JOIN p.category c "
            + "WHERE a.deleted = false "
            + "AND a.status = 'OPEN' "
            + "AND p.name LIKE %:keyword% "
            + "AND c.categoryId = :categoryId")
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Auction> findByKeywordAndCategoryId(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product p "
            + "WHERE a.deleted = false "
            + "AND a.status = 'OPEN' "
            + "AND p.name LIKE %:keyword%")
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Auction> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product.category c "
            + "WHERE a.deleted = false "
            + "AND a.status = 'OPEN' "
            + "AND c.categoryId = :categoryId")
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Auction> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "LEFT JOIN Bid b "
            + "ON b.auction = a "
            + "WHERE a.deleted = false "
            + "AND a.status = 'OPEN' "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC, "
            + "a.createdAt DESC")
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Auction> findAllOrderByBidCount(Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product p "
            + "JOIN p.category c "
            + "LEFT JOIN Bid b "
            + "ON b.auction = a "
            + "WHERE a.deleted = false "
            + "AND a.status = 'OPEN' "
            + "AND p.name LIKE %:keyword% "
            + "AND c.categoryId = :categoryId "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC")
    @EntityGraph(attributePaths = {"product", "product.category"})
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
            + "AND a.status = 'OPEN' "
            + "AND p.name LIKE %:keyword% "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC")
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Auction> findByKeywordOrderByBidCount(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a "
            + "FROM Auction a "
            + "JOIN a.product.category c "
            + "LEFT JOIN Bid b "
            + "ON b.auction = a "
            + "WHERE a.deleted = false "
            + "AND a.status = 'OPEN' "
            + "AND c.categoryId = :categoryId "
            + "GROUP BY a.auctionId "
            + "ORDER BY COUNT(b) DESC")
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Auction> findByCategoryIdOrderByBidCount(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT COUNT(a) FROM Auction a "
            + "WHERE a.product.seller.member.memberId = :sellerId "
            + "AND a.status = :status")
    Integer countBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                     @Param("status") AuctionStatus status);

    @EntityGraph(attributePaths = {"product"})
    List<Auction> findByAuctionIdIn(List<Long> auctionIds);

    @Query("""
            SELECT a.auctionId AS auctionId,
                   a.duration.startTime as startTime,
                   a.duration.endTime as endTime,
                   a.instantBidPrice as instantBidPrice,
                   a.status AS status,
                   p.name AS name,
                   p.thumbnailUrl AS thumbnailUrl,
                   p.category as category,
                   o.orderId.orderId as orderId
            FROM Auction a
            JOIN a.product p
            LEFT JOIN Order o ON o.auction.auctionId = a.auctionId
            WHERE a.product.seller.member.memberId = :memberId
            AND a.deleted = FALSE
            ORDER BY a.createdAt DESC
            """)
    Page<Tuple> findAllAuctionHistoryByMemberId(@Param("memberId") Long memberId,
                                                Pageable pageable);
}
