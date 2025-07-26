package com.deal4u.fourplease.domain.settlement.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends CrudRepository<Settlement, Long> {

    @Query("SELECT s.status "
            + "FROM Settlement s "
            + "WHERE s.auction.auctionId = :auctionId")
    SettlementStatus getSettlementStatusByAuctionId(@Param("auctionId") Long auctionId);

    boolean existsByAuctionAndBidder(Auction auction, Bidder bidder);

    @Query("SELECT s FROM Settlement s WHERE s.auction = :auction AND s.bidder = :bidder")
    Optional<Settlement> findByAuctionAndBidder(@Param("auction") Auction auction,
            @Param("bidder") Bidder bidder);

    @Query("SELECT s "
            + "FROM Settlement s "
            + "WHERE s.auction = :auction "
            + "AND s.status = :status")
    Optional<Settlement> findPendingSettlementByAuction(
            @Param("auction") Auction auction,
            @Param("status") SettlementStatus status
    );

    @Query("SELECT s "
            + "FROM Settlement s "
            + "WHERE s.settlementId = :settlementId "
            + "AND s.status = :status")
    Optional<Settlement> findPendingSettlementById(
            @Param("settlementId") Long settlementId,
            @Param("status") SettlementStatus status
    );
}
