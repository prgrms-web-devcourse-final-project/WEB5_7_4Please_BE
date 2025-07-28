package com.deal4u.fourplease.domain.bid.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBid;
import com.deal4u.fourplease.domain.member.mypage.dto.MyBidBase;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementInfo;
import jakarta.persistence.Tuple;
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
            SELECT b
            FROM Bid b
            WHERE b.auction.auctionId = :auctionId
              AND b.deleted = false
            ORDER BY b.price DESC
            LIMIT 2
            """)
    List<Bid> findTop2ByAuctionId(@Param("auctionId") Long auctionId);

    @Query("SELECT b "
            + "FROM Bid b "
            + "WHERE b.auction.auctionId = :auctionId "
            + "AND b.deleted = false "
            + "AND b.isSuccessfulBidder = false "
            + "AND b.price < (SELECT MAX(b2.price) FROM Bid b2 "
            + "WHERE b2.auction.auctionId = :auctionId AND b2.deleted = false) "
            + "ORDER BY b.price DESC, b.bidTime ASC")
    Optional<Bid> findSecondHighestBidByAuctionIdForSchedule(@Param("auctionId") Long auctionId);

    // 최고가 가져오기
    @Query("""
                        SELECT new com.deal4u.fourplease.domain.member.mypage.dto.HighestBid(
                            ba.auctionId,
                            MAX(b.price)
                        )
                        FROM Bid b
                        JOIN b.auction ba
                        WHERE b.deleted = false
                        AND ba.auctionId IN :auctionIds
                        GROUP BY ba.auctionId
            """
    )
    List<HighestBid> findHighestBidsForAuctionIds(@Param("auctionIds") List<Long> auctionIds);

    @Query("""
            SELECT b.bidTime as bidTime, b.auction.auctionId as auctionId
            FROM Bid b
            WHERE b.bidder.member.memberId = :memberId
            ORDER BY b.bidTime DESC
            """)
    Page<Tuple> findAllBidHistoryByMemberId(@Param("memberId") Long memberId,
                                            Pageable pageable);

    // 한번도 안해본 방법이라 성능은 잘 모르겠어욤
    //이런 느낌 어떠신가요? 저두욤 ㅎ
    //아니네요 ㄱㅊ 할것 같아ㅛ 해보시죠 그 실행계획 한번 잡아주실 수 있나요?

    /**
     * SELECT
     * MAX(b.bid_time) AS maxTime,
     * b.auction_id
     * FROM
     * bid b
     * JOIN
     * member m ON b.bidder_member_id = m.member_id
     * WHERE
     * m.member_id = :memberId
     * GROUP BY
     * b.auction_id
     * ORDER BY
     * maxTime DESC;
     * limit 10
     */
    //맞춰서 조금 수정해주세요
    //유한님 보실때 어때 보이시나요?
    //일단 만들어뒀습니다

    // 이제 settlement랑 shipment는 어케 가져오나요?
    // 저번에 유한님이 만든것 처럼 service에서 핸들링 할가요?
    // 일단 찾아볼게요 ㅎㅎ 어딨지... ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ 만들고 리펙토링 하죠
    /*
    -> Limit: 10 row(s)  (actual time=90.5..90.5 rows=10 loops=1)
    -> Sort: maxTime DESC, limit input to 10 row(s) per chunk  (actual time=90.5..90.5 rows=10 loops=1)
        -> Table scan on <temporary>  (actual time=88.8..89.6 rows=10001 loops=1)
            -> Aggregate using temporary table  (actual time=88.8..88.8 rows=10001 loops=1)
                -> Index lookup on b using FK5af976l09i5v2jv9kgxp8te8x (bidder_member_id = 2)  (cost=15239 rows=106456) (actual time=7.36..80.4 rows=50001 loops=1)
     */
    @Query("""
                SELECT new com.deal4u.fourplease.domain.member.mypage.dto.MyBidBase(
                    a.auctionId,
                    a.startingPrice,
                    a.instantBidPrice,
                    a.status,
                    p.seller,
                    p.name,
                    p.thumbnailUrl,
                    b.bidId,
                    b.price,
                    b.bidTime,
                    b.isSuccessfulBidder,
                    s.status,
                    s.paymentDeadline,
                    sh.status,
                    maxBid.highestBid
                )
                FROM Bid b
                JOIN b.auction a
                JOIN a.product p
                JOIN b.bidder bd
                JOIN bd.member m
                LEFT JOIN Settlement s ON s.auction.auctionId = a.auctionId 
                            AND s.bidder.member.memberId = :memberId
                LEFT JOIN Shipment sh ON sh.auction.auctionId = a.auctionId
                LEFT JOIN (
                        SELECT ba.auctionId AS auctionId, MAX(bb.price) AS highestBid
                        FROM Bid bb
                        JOIN bb.auction ba
                        WHERE bb.deleted = false
                        GROUP BY ba.auctionId
                    ) maxBid ON maxBid.auctionId = a.auctionId
                    WHERE m.memberId = :memberId
                    AND b.deleted = false
                    ORDER BY b.bidTime DESC
            """)
    Page<MyBidBase> findMyBidHistory(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    @Query("""
                SELECT new com.deal4u.fourplease.domain
                            .member.mypage.dto.SettlementInfo(
                    s.auction.auctionId,
                    s.status,
                    s.paymentDeadline,
                    sh.status
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
}
