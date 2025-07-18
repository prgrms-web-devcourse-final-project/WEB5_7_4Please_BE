package com.deal4u.fourplease.domain.bid.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.HighestBidInfo;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistoryBase;
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

    @Query(
            value = """
                        SELECT new com.deal4u.fourplease.domain
                                            .member.mypage.dto.MyPageBidHistoryBase(
                            a.auctionId,
                            b.bidId,
                            p.thumbnailUrl,
                            p.name,
                            CAST(a.status AS string),
                            CAST(a.startingPrice AS double),
                            CAST(a.instantBidPrice AS double),
                            CAST(b.price AS double),
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
                        WHERE m.memberId = :memberId AND b.deleted = false
                        ORDER BY b.bidTime DESC
                    """
    )
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
}
