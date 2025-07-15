package com.deal4u.fourplease.domain.auction.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import java.math.BigDecimal;
import java.util.Optional;
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


    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.auctionId = :auctionId "
            + "AND a.deleted = false "
            + "AND a.status = 'CLOSED'")
    Optional<Auction> findByAuctionIdAndDeletedFalseAndStatusClosed(Long auctionId);

    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auction.auctionId = :auctionId AND b.deleted = false")
    Optional<BigDecimal> findMaxBidPriceByAuctionId(Long auctionId);
}
