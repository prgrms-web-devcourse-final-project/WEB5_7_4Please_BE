package com.deal4u.fourplease.domain.settlement.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SettlementRepository extends CrudRepository<Settlement, Long> {
    boolean existsByAuctionAndBidderAndStatus(Auction auction, Bidder bidder,
                                              SettlementStatus status);


    @Query("select s.status "
            + "from Settlement s "
            + "where s.auction.auctionId = :auctionId")
    SettlementStatus getSettlementStatusByAuctionId(Long auctionId);

}
