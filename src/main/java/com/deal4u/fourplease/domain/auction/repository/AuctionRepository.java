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

    @Query("select a from Auction a join fetch a.product where a.auctionId = :auctionId")
    Optional<Auction> findByIdWithProduct(@Param("auctionId") Long auctionId);

    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.auctionId = :auctionId "
            + "AND a.deleted = false "
            + "AND a.status = 'OPEN'")
    Optional<Auction> findByAuctionIdAndDeletedFalseAndStatusOpen(Long auctionId);

    @Query("select a "
            + "from Auction a "
            + "join fetch a.product p "
            + "where p.productId in :productIdList "
            + "and a.deleted = false "
            + "order by a.createdAt desc")
    Page<Auction> findAllByProductIdIn(
            @Param("productIdList") List<Long> productIdList,
            Pageable pageable
    );
}
