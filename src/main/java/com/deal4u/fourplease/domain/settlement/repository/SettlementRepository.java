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

    @Query("select s.status "
            + "from Settlement s "
            + "where s.auction.auctionId = :auctionId")
    SettlementStatus getSettlementStatusByAuctionId(Long auctionId);


    @Query("SELECT s FROM Settlement s WHERE s.auction = :auction AND s.bidder = :bidder")
    Optional<Settlement> findByAuctionAndBidder(@Param("auction") Auction auction,
                                                @Param("bidder") Bidder bidder);

}
