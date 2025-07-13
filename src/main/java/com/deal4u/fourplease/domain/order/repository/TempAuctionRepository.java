package com.deal4u.fourplease.domain.order.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// Todo: 실제 레포지토리 변경 및 삭제 필요
public interface TempAuctionRepository extends JpaRepository<Auction, Long> {

    @Query("SELECT a "
            + "FROM Auction a "
            + "WHERE a.auctionId = :auctionId "
            + "AND a.deleted = false "
            + "AND a.status = 'OPEN'")
    Optional<Auction> findByAuctionIdAndDeletedFalseAndStatusClosed(Long auctionId);
}
