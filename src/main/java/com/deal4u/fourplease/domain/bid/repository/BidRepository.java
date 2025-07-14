package com.deal4u.fourplease.domain.bid.repository;

import com.deal4u.fourplease.domain.bid.entity.Bid;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("select b.price from Bid b where b.auction.auctionId = :auctionId order by b.price desc")
    List<Long> findPricesByAuctionIdOrderByPriceDesc(@Param("auctionId") Long auctionId);

}
