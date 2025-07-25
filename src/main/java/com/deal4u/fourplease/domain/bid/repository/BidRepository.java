package com.deal4u.fourplease.domain.bid.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBidInfo;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryBase;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryComplete;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {


    @Query("SELECT b "
            + "FROM Bid b "
            + "WHERE b.auction.auctionId = :auctionId "
            + "AND b.bidder.member = :member "
            + "AND b.isSuccessfulBidder = true")
    Optional<Bid> findSuccessfulBid(@Param("auctionId") Long auctionId,
                                    @Param("member") Member member);

    Optional<Bid> findTopByAuctionAndBidderOrderByPriceDesc(Auction auction, Bidder bidder);

    Optional<Bid> findTopByAuctionOrderByPriceDescBidTimeAsc(Auction auction);

    @Query("SELECT b.price "
            + "FROM Bid b "
            + "WHERE b.deleted = false "
            + "AND b.auction.auctionId = :auctionId "
            + "ORDER BY b.price DESC")
    List<BigDecimal> findPricesByAuctionIdOrderByPriceDesc(@Param("auctionId") Long auctionId);

    @Query("SELECT b "
            + "FROM Bid b "
            + "WHERE b.auction.auctionId = :auctionId "
            + "AND b.bidder.member = :member "
            + "AND b.isSuccessfulBidder = true")
    Optional<Bid> findSuccessFulBid(@Param("auctionId") Long auctionId,
                                    @Param("member") Member member);

    @Query("SELECT MAX(b.price) "
            + "FROM Bid b "
            + "WHERE b.auction.auctionId = :auctionId "
            + "AND b.deleted = false")
    Optional<BigDecimal> findMaxBidPriceByAuctionId(Long auctionId);

    Optional<Bid> findByBidIdAndBidder(Long bidId, Bidder bidder);

    Optional<Bid> findByAuctionAndBidder(Auction auction, Bidder bidder);

    @SuppressWarnings("checkstyle:MethodName")
    Page<Bid> findByAuctionAndDeletedFalseOrderByPriceDescBidTimeAsc(Auction auction,
                                                                     Pageable pageable);

    @Query("""
                SELECT new com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryBase(
                    a.auctionId,
                    b.bidId,
                    p.thumbnailUrl,
                    p.name,
                    a.status,
                    a.startingPrice,
                    a.instantBidPrice,
                    b.price,
                    b.isSuccessfulBidder,
                    b.bidTime,
                    b.createdAt,
                    p.seller.member.nickName
                )
                FROM Bid b
                JOIN b.auction a
                JOIN a.product p
                JOIN b.bidder bd
                JOIN bd.member m
                WHERE m.memberId = :memberId 
                AND b.deleted = false
                ORDER BY b.bidTime DESC
            """)
    Page<MyPageBidHistoryBase> findMyBidHistoryBase(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    // 결제/배송 상태 조회
    @Query("""
                SELECT new com.deal4u.fourplease.domain
                            .member.mypage.dto.SettlementInfo(
                    s.auction.auctionId,
                    CAST(s.status AS string),
                    s.paymentDeadline,
                    CAST(sh.status AS string)
                )
                FROM Settlement s
                LEFT JOIN Shipment sh ON sh.auction = s.auction
                WHERE s.bidder.member.memberId = :memberId
                AND s.auction.auctionId IN :auctionIds
            """)
    List<SettlementInfo> findSettlementInfoByAuctionIds(
            @Param("memberId") Long memberId,
            @Param("auctionIds") List<Long> auctionIds
    );

    @Query("""
            SELECT b
            FROM Bid b
            WHERE b.auction.auctionId = :auctionId
              AND b.deleted = false
            ORDER BY b.price DESC
            LIMIT 2
            """)
    List<Bid> findTop2ByAuctionId(@Param("auctionId") Long auctionId);

    // 최고가 입찰 정보 조회
    @Query("""
                SELECT new com.deal4u.fourplease.domain
                            .member.mypage.dto.HighestBidInfo(
                    a.auctionId,
                    CAST(MAX(b.price) AS double)
                )
                FROM Bid b
                JOIN b.auction a
                WHERE a.auctionId IN :auctionIds
                AND b.deleted = false
                GROUP BY a.auctionId
            """)
    List<HighestBidInfo> findHighestBidInfoByAuctionIds(@Param("auctionIds") List<Long> auctionIds);


    @Query("""
                SELECT new com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryComplete(
                    a.auctionId,
                    b.bidId,
                    p.thumbnailUrl,
                    p.name,
                    a.status,
                    a.startingPrice,
                    a.instantBidPrice,
                    b.price,
                    b.isSuccessfulBidder,
                    b.bidTime,
                    b.createdAt,
                    p.seller.member.nickName,
                    s.status,
                    s.paymentDeadline,
                    sh.status,
                    maxBid.highestPrice
                )
                FROM Bid b
                JOIN b.auction a
                JOIN a.product p
                JOIN b.bidder bd
                JOIN bd.member m
                LEFT JOIN Settlement s ON s.auction = a AND s.bidder.member.memberId = :memberId
                LEFT JOIN Shipment sh ON sh.auction = a
                LEFT JOIN (
                    SELECT ba.auctionId AS auctionId, MAX(bb.price) AS highestPrice
                    FROM Bid bb 
                    JOIN bb.auction ba
                    WHERE bb.deleted = false
                    GROUP BY ba.auctionId
                ) maxBid ON maxBid.auctionId = a.auctionId
                WHERE m.memberId = :memberId 
                AND b.deleted = false
                ORDER BY b.bidTime DESC
            """)
    Page<MyPageBidHistoryComplete> findMyBidHistoryComplete(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    @Query("SELECT b "
            + "FROM Bid b "
            + "WHERE b.auction.auctionId = :auctionId "
            + "AND b.deleted = false "
            + "AND b.isSuccessfulBidder = false "
            + "AND b.price < (SELECT MAX(b2.price) FROM Bid b2 "
            + "WHERE b2.auction.auctionId = :auctionId AND b2.deleted = false) "
            + "ORDER BY b.price DESC, b.bidTime ASC")
    Optional<Bid> findSecondHighestBidByAuctionIdForSchedule(@Param("auctionId") Long auctionId);
}
