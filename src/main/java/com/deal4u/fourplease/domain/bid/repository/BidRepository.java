package com.deal4u.fourplease.domain.bid.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.CountBid;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBid;
import com.deal4u.fourplease.domain.member.mypage.dto.MyBidBase;
import com.deal4u.fourplease.domain.member.mypage.dto.SettlementInfo;
import com.deal4u.fourplease.domain.member.mypage.dto.SuccessfulBidder;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
    @EntityGraph(attributePaths = {"bidder.member"})
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

    // 입찰수 가져오기
    @Query("""
                        SELECT new com.deal4u.fourplease.domain.member.mypage.dto.CountBid(
                            ba.auctionId,
                            COUNT(b.bidId)
                        )
                        FROM Bid b
                        JOIN b.auction ba
                        WHERE b.deleted = false
                        AND ba.auctionId IN :auctionIds
                        GROUP BY ba.auctionId
            """
    )
    List<CountBid> findCountBidsForAuctionIds(@Param("auctionIds") List<Long> auctionIds);

    // 낙찰자 가져오기
    @Query("""
                        SELECT new com.deal4u.fourplease.domain.member.mypage.dto.SuccessfulBidder(
                            ba.auctionId,
                            b.bidId,
                            b.bidder.member.nickName
                        )
                        FROM Bid b
                        JOIN b.auction ba
                        JOIN b.bidder bidder
                        JOIN bidder.member m
                        WHERE b.deleted = false
                        AND b.isSuccessfulBidder = true
                        AND ba.auctionId IN :auctionIds
            """
    )
    List<SuccessfulBidder> findSuccessfulBidderForAuctionIds(
            @Param("auctionIds") List<Long> auctionIds);

    @Query("""
            SELECT b.bidTime as bidTime,
                   b.auction.auctionId as auctionId,
                   b.bidId as bidId,
                   b.price as myBidPrice,
                   b.isSuccessfulBidder as isSuccessfulBidder
            FROM Bid b
            WHERE b.bidder.member.memberId = :memberId
            ORDER BY b.bidTime DESC
            """)
    Page<Tuple> findAllBidHistoryByMemberId(@Param("memberId") Long memberId,
                                            Pageable pageable);

    @Query("""
                SELECT new com.deal4u.fourplease.domain.member.mypage.dto.MyBidBase(
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null ,
                    null 
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

    @Query("SELECT b.auction.auctionId, b.price "
            + "FROM Bid b "
            + "WHERE b.auction.auctionId IN :auctionIds")
    List<Object[]> findPricesByAuctionIds(@Param("auctionIds") List<Long> auctionIds);

    // findPricesByAuctionIdsGrouped를 직접 가공
    default Map<Long, List<BigDecimal>> findPricesByAuctionIdsGrouped(List<Long> auctionIds) {
        Map<Long, List<BigDecimal>> result = new HashMap<>();
        List<Object[]> rows = findPricesByAuctionIds(auctionIds);

        for (Object[] row : rows) {
            Long auctionId = (Long) row[0];
            BigDecimal price = (BigDecimal) row[1];
            // key auctionId가 result에 존재하면 기존 값, 없으면 빈 리스트 생성해서 map에 추가
            result.computeIfAbsent(auctionId, k -> new ArrayList<>()).add(price);
        }

        // 각 가격 내림차순 정렬
        result.values().forEach(list -> list.sort(Comparator.reverseOrder()));

        return result;
    }
}
