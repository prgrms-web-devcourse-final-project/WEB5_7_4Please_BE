package com.deal4u.fourplease.domain.bid.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findByAuctionAndBidder(Auction auction, Bidder bidder);

    @Query("select b.price from Bid b where b.auction.auctionId = :auctionId order by b.price desc")
    List<Long> findPricesByAuctionIdOrderByPriceDesc(@Param("auctionId") Long auctionId);

    Optional<Bid> findByBidIdAndBidder(Long bidId, Bidder bidder);


}
