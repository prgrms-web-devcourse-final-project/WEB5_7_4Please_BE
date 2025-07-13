package com.deal4u.fourplease.domain.order.repository;

import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// todo: 실제 레포지토리로 변경 필요
public interface TempBidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT b FROM Bid b "
            + "WHERE b.auction.auctionId = :auctionId "
            + "AND b.bidder.member = :member "
            + "AND b.isSuccessFulBidder = true")
    Optional<Bid> findSuccessfulBid(@Param("auctionId") Long auctionId,
                                    @Param("member") Member member);
}
