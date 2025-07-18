package com.deal4u.fourplease.domain.bid.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
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

    Optional<Bid> findTopByAuctionAndBidderOrderByPriceDesc(Auction auction, Bidder bidder);

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

    @SuppressWarnings("checkstyle:MethodName")
    Page<Bid> findByAuctionAndDeletedFalseOrderByPriceDescBidTimeAsc(Auction auction,
                                                                     Pageable pageable);

    // h2 방식!! 중요!!,
    @Query(
            value = """
                        SELECT new com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory(
                            a.auctionId,
                            b.bidId,
                            COALESCE(p.thumbnailUrl, ''),
                            COALESCE(p.name, ''),
                            CASE
                                WHEN s.status IS NOT NULL THEN
                                    CASE
                                        WHEN s.status = 'SUCCESS' THEN 
                                            COALESCE(sh.status, '결제 완료')
                                        WHEN s.status = 'PENDING' THEN '낙찰'
                                        WHEN s.status = 'REJECTED' THEN '결제 실패'
                                        ELSE '낙찰'
                                    END
                                WHEN a.status = 'FAIL' AND s.status IS NULL THEN '패찰'
                                WHEN a.status = 'CLOSED' THEN 
                                    CASE WHEN b.isSuccessfulBidder THEN '낙찰' ELSE '경매 종료' END
                                ELSE '진행중'
                            END AS status,
                            COALESCE(a.startingPrice, 0.0),
                            COALESCE(MAX(b2.price), 0.0),
                            COALESCE(a.instantBidPrice, 0.0),
                            COALESCE(b.price, 0.0),
                            COALESCE(b3.bidder.member.nickName, ''),
                            COALESCE(b3.price, 0.0),
                            b.bidTime,
                            b.createdAt,
                            COALESCE(
                                CAST(FORMATDATETIME(
                                    (SELECT s2.paymentDeadline FROM Settlement s2 WHERE s2.auction = a AND s2.bidder.member.memberId = :memberId),
                                    'yyyy-MM-dd HH:mm') AS string),
                                ''
                            ),
                            COALESCE(p.seller.member.nickName, '')
                        )
                        FROM Bid b
                        JOIN b.auction a
                        JOIN a.product p
                        JOIN b.bidder bd
                        JOIN bd.member m
                        LEFT JOIN Settlement s ON s.auction = a AND s.bidder.member.memberId = :memberId
                        LEFT JOIN Shipment sh ON sh.auction = a
                        LEFT JOIN Bid b3 ON b3.auction = a AND b3.isSuccessfulBidder = true AND b3.deleted = false
                        WHERE m.memberId = :memberId AND b.deleted = false
                        GROUP BY a.auctionId, b.bidId, p.thumbnailUrl, p.name, b.price, b.bidTime, b.createdAt, p.seller.member.nickName
                        ORDER BY b.bidTime DESC
                    """,
            countQuery = """
                        SELECT COUNT(b)
                        FROM Bid b
                        WHERE b.bidder.member.memberId = :memberId AND b.deleted = false
                    """
    )
    Page<MyPageBidHistory> findMyBidHistoryH2(@Param("memberId") Long memberId, Pageable pageable);

    // mysql 용
    @Query(
            value = """
                            SELECT new com.deal4u.fourplease.domain.
                                                member.mypage.dto.MyPageBidHistory(
                                a.auctionId,
                                b.bidId,
                                COALESCE(p.thumbnailUrl, ''),
                                COALESCE(p.name, ''),
                    
                                CASE
                                    WHEN s.status IS NOT NULL THEN
                                        CASE
                                            WHEN s.status = 'SUCCESS' THEN '결제 완료'
                                            WHEN s.status = 'PENDING' THEN '결제 대기'
                                            WHEN s.status = 'REJECTED' THEN '결제 실패'
                                            ELSE '낙찰'
                                        END
                                    ELSE
                                        CASE
                                            WHEN a.status = 'FAIL' THEN '패찰'
                                            ELSE '몰라'
                                        END
                                END,
                    
                                COALESCE(a.startingPrice, 0.0),
                                COALESCE(
                                    (SELECT MAX(b2.price)
                                     FROM Bid b2
                                     WHERE b2.auction = a AND b2.deleted = false), 0.0),
                                COALESCE(a.instantBidPrice, 0.0),
                                COALESCE(b.price, 0.0),
                    
                                COALESCE(
                                    (SELECT b3.bidder.member.nickName
                                     FROM Bid b3
                                     WHERE b3.auction = a
                                     AND b3.isSuccessfulBidder = true
                                     AND b3.deleted = false), ''),
                                COALESCE(
                                    (SELECT b3.price
                                     FROM Bid b3
                                     WHERE b3.auction = a
                                     AND b3.isSuccessfulBidder = true
                                     AND b3.deleted = false), 0.0),
                    
                                b.bidTime,
                                b.createdAt,
                    
                                COALESCE(CAST(
                                    DATE_FORMAT(
                                        (SELECT s2.paymentDeadline
                                         FROM Settlement s2
                                         WHERE s2.auction = a
                                         AND s2.bidder.member.memberId = :memberId),
                                                             '%Y-%m-%d %H:%i')
                                    AS string), ''),
                    
                                COALESCE(p.seller.member.nickName, '')
                            )
                            FROM Bid b
                            JOIN b.auction a
                            JOIN a.product p
                            JOIN b.bidder bd
                            JOIN bd.member m
                            LEFT JOIN Settlement s
                                ON s.auction = a AND s.bidder.member.memberId = :memberId
                            WHERE m.memberId = :memberId AND b.deleted = false
                            ORDER BY b.bidTime DESC
                    """,
            countQuery = """
                            SELECT COUNT(b)
                            FROM Bid b
                            WHERE b.bidder.member.memberId = :memberId AND b.deleted = false
                    """
    )
    Page<MyPageBidHistory> findMyBidHistory(
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
