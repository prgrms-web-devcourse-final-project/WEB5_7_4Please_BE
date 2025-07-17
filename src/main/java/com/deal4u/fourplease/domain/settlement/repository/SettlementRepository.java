package com.deal4u.fourplease.domain.settlement.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends CrudRepository<Settlement, Long> {

    @Query("select s.status "
            + "from Settlement s "
            + "where s.auction.auctionId = :auctionId")
    SettlementStatus getSettlementStatusByAuctionId(Long auctionId);


    @Query("SELECT s FROM Settlement s "
            + "JOIN FETCH s.auction a "
            + "WHERE a = :auction AND a.status = 'CLOSED'")
    Optional<Settlement> findByAuctionWithJoin(Auction auction);

    @Query("SELECT s FROM Settlement s "
            + "JOIN FETCH s.auction "
            + "WHERE s.status = :status "
            + "AND s.paymentDeadline < :currentTime")
    List<Settlement> findExpiredSettlements(@Param("status") SettlementStatus status,
                                            @Param("currentTime") LocalDateTime currentTime);

    boolean existsByAuctionAndBidder(Auction auction, Bidder bidder);

    Optional<Settlement> findByAuction(Auction auction);

    List<Settlement> findByStatus(SettlementStatus status);


}
