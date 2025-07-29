//package com.deal4u.fourplease;
//
//import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Queue;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.jdbc.core.BatchPreparedStatementSetter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//
//@SuppressWarnings("checkstyle:Indentation")
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class InitProcess implements CommandLineRunner {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @Override
//    public void run(String... args) throws Exception {
//        int threadCount = 4;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        initProduct10000(threadCount, executorService);
//        initAuctions10000(threadCount, executorService);
//        initBid1000000(threadCount, executorService);
//        initOrders20000(threadCount, executorService);
//        initPayments20000(threadCount, executorService);
//        initReviews50000(threadCount, executorService);
//        initWishlist30000(threadCount, executorService);
//        executorService.shutdown();
//    }
//
//    private void initBid1000000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        AtomicInteger count = new AtomicInteger(0);
//        Queue<AuctionPair> auctionPairs = getAuctionPairs();
//        for (int i = 0; i < 100; i++) {
//            initBid10000(threadCount, executorService, auctionPairs, count);
//        }
//    }
//
//    private void initBid10000(int threadCount, ExecutorService executorService,
//            Queue<AuctionPair> pairs, AtomicInteger count)
//            throws InterruptedException {
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                jdbcTemplate.batchUpdate(
//                        """
//                                insert into bid(
//                                                auction_id,
//                                                bidder_member_id,
//                                                price,
//                                                BID_TIME,
//                                                is_successful_bidder,
//                                                deleted,
//                                                created_at,
//                                                updated_at)
//                                values (?,?,?,?,?,false,now(),now())
//                                """,
//                        new BatchPreparedStatementSetter() {
//                            @Override
//                            public void setValues(PreparedStatement ps, int i)
//                                    throws SQLException {
//                                AuctionPair auctionPair = null;
//                                int countValue = -1;
//                                synchronized (pairs) {
//                                    countValue = count.incrementAndGet();
//                                    if (100 > countValue) {
//                                        auctionPair = pairs.peek();
//                                    } else {
//                                        pairs.poll();
//                                        auctionPair = pairs.peek();
//                                        count.set(0);
//                                        countValue = 0;
//                                    }
//                                }
//
//                                if (AuctionStatus.CLOSE == auctionPair.auctionStatus) {
//                                    ps.setLong(1, auctionPair.auctionId());
//                                    ps.setInt(2, (countValue % 20) + 2);
//                                    ps.setInt(3, countValue + 10000);
//                                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
//                                    if (count.get() == 99) {
//                                        ps.setBoolean(5, true);
//                                    } else {
//                                        ps.setBoolean(5, false);
//                                    }
//                                } else if (AuctionStatus.OPEN == auctionPair.auctionStatus) {
//                                    ps.setLong(1, auctionPair.auctionId());
//                                    ps.setInt(2, (countValue % 20) + 2);
//                                    ps.setInt(3, countValue + 10000);
//                                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
//                                    ps.setBoolean(5, false);
//                                } else {
//                                    ps.setLong(1, auctionPair.auctionId());
//                                    ps.setInt(2, (countValue % 20) + 2);
//                                    ps.setInt(3, countValue + 10000);
//                                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
//                                    ps.setBoolean(5, false);
//                                }
//                            }
//
//                            @Override
//                            public int getBatchSize() {
//                                return 2500;
//                            }
//                        });
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    private void initAuctions10000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//        AtomicInteger atomicInteger = new AtomicInteger(0);
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                jdbcTemplate.batchUpdate("""
//                                insert into auctions(
//                                                     PRODUCT_PRODUCT_ID,
//                                                     starting_price,
//                                                     start_time,
//                                                     end_time,
//                                                     status,
//                                                     DELETED,
//                                                     updated_at,
//                                                     created_at)
//                                values (?,1000,now(),now(),?,false,now(),now())
//                                """,
//                        new BatchPreparedStatementSetter() {
//                            @Override
//                            public void setValues(PreparedStatement ps, int i)
//                                    throws SQLException {
//                                int value = atomicInteger.incrementAndGet();
//                                ps.setLong(1, value + 2);
//                                if (value <= 7000) {
//                                    ps.setString(2, "CLOSE");
//                                } else if (value <= 9900) {
//                                    ps.setString(2, "OPEN");
//                                } else {
//                                    ps.setString(2, "FAIL");
//                                }
//                            }
//
//                            @Override
//                            public int getBatchSize() {
//                                return 2500;
//                            }
//                        });
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    private void initProduct10000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                try {
//                    jdbcTemplate.batchUpdate("""
//                                    insert into products(
//                                                         name,
//                                                         description,
//                                                         thumbnail_url,
//                                                         address,
//                                                         DETAIL_ADDRESS,
//                                                         zip_code,
//                                                         SELLER_MEMBER_ID,
//                                                         CATEGORY_CATEGORY_ID,
//                                                         phone,deleted,
//                                                         created_at,updated_at)
//                                    values (
//                                            'testProduct',
//                                            '',
//                                            '',
//                                            '',
//                                            '',
//                                            '',
//                                            1,
//                                            1,
//                                            '',
//                                            false,
//                                            now(),
//                                            now())
//                                    """,
//                            new BatchPreparedStatementSetter() {
//                                @Override
//                                public void setValues(PreparedStatement ps, int i)
//                                        throws SQLException {
//                                }
//
//                                @Override
//                                public int getBatchSize() {
//                                    return 2500;
//                                }
//
//                            });
//                } catch (Exception throwables) {
//                    throwables.printStackTrace();
//                }
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    // InitProcess 클래스에 추가할 메서드들
//
//    private void initSettlements7000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        // CLOSE 상태인 경매들의 ID를 가져와서 정산 데이터 생성
//        List<Long> closedAuctionIds = getClosedAuctionIds();
//
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//        AtomicInteger atomicInteger = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                jdbcTemplate.batchUpdate("""
//                                insert into settlement(
//                                                 auction_id,
//                                                 bidder_member_id,
//                                                 status,
//                                                 payment_deadline,
//                                                 rejected_reason,
//                                                 paid_at,
//                                                 deleted,
//                                                 created_at,
//                                                 updated_at)
//                                values (?,?,?,?,?,?,false,now(),now())
//                                """,
//                        new BatchPreparedStatementSetter() {
//                            @Override
//                            public void setValues(PreparedStatement ps, int i)
//                                    throws SQLException {
//                                int index = atomicInteger.incrementAndGet();
//
//                                // CLOSE 상태 경매 중에서 순차적으로 선택
//                                Long auctionId =
//                                        closedAuctionIds.get(index % closedAuctionIds.size());
//
//                                // 낙찰자는 각 경매의 성공한 입찰자들 중에서 선택
//                                ps.setLong(1, auctionId); // auction_id
//                                ps.setInt(2, (index % 20) + 2); // bidder_member_id (2~21)
//
//                                SettlementData settlementData = generateSettlementData(index);
//                                ps.setString(3, settlementData.status()); // status
//                                ps.setTimestamp(4,
//                                        settlementData.paymentDeadline()); // payment_deadline
//                                ps.setString(5, settlementData.rejectedReason());
//                                ps.setTimestamp(6, settlementData.paidAt()); // paid_at
//                            }
//
//                            @Override
//                            public int getBatchSize() {
//                                return 1750; // 7000개를 4개 스레드로 나누면 각각 1750개
//                            }
//                        });
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    private SettlementData generateSettlementData(int index) {
//        // 80% SUCCESS, 15% PENDING, 5% REJECTED
//        int statusRandom = index % 100;
//        String status;
//        Timestamp paymentDeadline = Timestamp.valueOf(LocalDateTime.now().plusDays(7)); // 7일 후
//        String rejectedReason = null;
//        Timestamp paidAt = null;
//
//        if (statusRandom < 80) {
//            // SUCCESS 상태
//            status = "SUCCESS";
//            paidAt = Timestamp.valueOf(LocalDateTime.now().minusDays(index % 5)); // 0~4일 전에 결제 완료
//        } else if (statusRandom < 95) {
//            // PENDING 상태
//            status = "PENDING";
//            paymentDeadline =
//                    Timestamp.valueOf(LocalDateTime.now().plusDays(3 + (index % 5))); // 3~7일 후 마감
//        } else {
//            // REJECTED 상태
//            status = "REJECTED";
//            rejectedReason = generateRejectedReason(index);
//            paymentDeadline = Timestamp.valueOf(LocalDateTime.now().minusDays(1)); // 마감일 지남
//        }
//
//        return new SettlementData(status, paymentDeadline, rejectedReason, paidAt);
//    }
//
//    private String generateRejectedReason(int index) {
//        String[] reasons = {
//                "결제 기한 초과",
//                "부정 거래 의심",
//                "계정 인증 실패",
//                "신용카드 결제 실패",
//                "사용자 요청에 의한 취소",
//                "판매자 요청에 의한 거래 취소"
//        };
//        return reasons[index % reasons.length];
//    }
//
//    // Settlement 데이터를 담는 record 클래스
//    private record SettlementData(
//            String status,
//            Timestamp paymentDeadline,
//            String rejectedReason,
//            Timestamp paidAt
//    ) {
//
//    }
//
//    private void initOrders20000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        List<Long> closedAuctionIds = getClosedAuctionIds();
//
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//        AtomicInteger atomicInteger = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                jdbcTemplate.batchUpdate("""
//                                insert into orders(
//                                                 order_id,
//                                                 member_member_id,
//                                                 auction_auction_id,
//                                                 price,
//                                                 address,
//                                                 address_detail,
//                                                 zip_code,
//                                                 phone,
//                                                 content,
//                                                 receiver,
//                                                 order_status,
//                                                 order_type,
//                                                 created_at,
//                                                 updated_at
//                                                 )
//                                values (?,?,?,?,?,?,?,?,?,?,?,?,now(),now())
//                                """,
//                        new BatchPreparedStatementSetter() {
//                            @Override
//                            public void setValues(PreparedStatement ps, int i)
//                            throws SQLException {
//                                int index = atomicInteger.incrementAndGet();
//
//                                // CLOSE 상태 경매 중에서 랜덤하게 선택
//                                Long auctionId =
//                                        closedAuctionIds.get(index % closedAuctionIds.size());
//
//                                ps.setString(1, generateOrderId(index)); // order_id
//                                ps.setInt(2, (index % 20) + 2); // member_id (2~21)
//                                ps.setLong(3, auctionId); // auction_id
//                                ps.setBigDecimal(4,
//                                new java.math.BigDecimal(10000 + (index % 50000))); // price
//                                ps.setString(5, generateAddress(index)); // address
//                                ps.setString(6, generateAddressDetail(index)); // address_detail
//                                ps.setString(7, generateZipCode(index)); // zip_code
//                                ps.setString(8, generatePhone(index)); // phone
//                                ps.setString(9, generateOrderContent(index)); // content
//                                ps.setString(10, generateReceiver(index)); // receiver
//                                ps.setString(11, generateOrderStatus(index)); // order_status
//                                ps.setString(12, generateOrderType(index)); // order_type
//                            }
//
//                            @Override
//                            public int getBatchSize() {
//                                return 2000;
//                            }
//                        });
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    private String generateOrderId(int index) {
//        return "ORD" + String.format("%08d", index);
//    }
//
//    private String generateAddress(int index) {
//        String[] addresses = {
//                "서울특별시 강남구 테헤란로",
//                "서울특별시 서초구 반포대로",
//                "서울특별시 송파구 올림픽로",
//                "서울특별시 마포구 홍익로",
//                "서울특별시 용산구 이태원로",
//                "경기도 성남시 분당구 판교로",
//                "경기도 수원시 영통구 광교로",
//                "경기도 고양시 일산동구 중앙로",
//                "부산광역시 해운대구 해운대해변로",
//                "대구광역시 수성구 동대구로"
//        };
//        return addresses[index % addresses.length];
//    }
//
//    private String generateAddressDetail(int index) {
//        return (index % 999 + 1) + "동 " + (index % 99 + 1) + "호";
//    }
//
//    private String generateZipCode(int index) {
//        return String.format("%05d", 10000 + (index % 89999));
//    }
//
//    private String generatePhone(int index) {
//        return "010-" + String.format("%04d", 1000 + (index % 8999)) + "-"
//                + String.format("%04d", 1000 + (index % 8999));
//    }
//
//    private String generateOrderContent(int index) {
//        String[] contents = {
//                "문 앞에 놓아주세요",
//                "경비실에 맡겨주세요",
//                "직접 받겠습니다",
//                "부재시 안전한 곳에 보관해주세요",
//                "배송 전 연락 부탁드립니다",
//                "조심히 다뤄주세요",
//                "빠른 배송 부탁드립니다",
//                "포장 꼼꼼히 해주세요",
//                "배송완료 문자 보내주세요",
//                "특별한 요청사항 없습니다"
//        };
//        return contents[index % contents.length];
//    }
//
//    private String generateReceiver(int index) {
//        String[] receivers = {
//                "김철수", "이영희", "박지민", "최수진", "정민호",
//                "강서연", "윤도현", "임지은", "오태준", "한미영",
//                "장준혁", "신예린", "권대성", "조수현", "배준서"
//        };
//        return receivers[index % receivers.length];
//    }
//
//    private String generateOrderStatus(int index) {
//        int statusRandom = index % 100;
//        if (statusRandom < 80) {
//            return "SUCCESS";
//        } else if (statusRandom < 95) {
//            return "PENDING";
//        } else {
//            return "FAILED";
//        }
//    }
//
//    private String generateOrderType(int index) {
//        return (index % 10 == 0) ? "BUY_NOW" : "AWARD";
//    }
//
//
//    private void initPayments20000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        // 생성된 주문들의 OrderId를 가져와서 결제 데이터 생성
//        List<String> orderIds = getOrderIds();
//
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//        AtomicInteger atomicInteger = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                jdbcTemplate.batchUpdate("""
//                                insert into payment(
//                                                 amount,
//                                                 status,
//                                                 payment_key,
//                                                 order_id,
//                                                 created_at,
//                                                 updated_at)
//                                values (?,?,?,?,now(),now())
//                                """,
//                        new BatchPreparedStatementSetter() {
//                            @Override
//                            public void setValues(PreparedStatement ps, int i)
//                            throws SQLException {
//                                int index = atomicInteger.incrementAndGet();
//
//                                // 주문 ID 중에서 순차적으로 선택 (각 주문마다 하나의 결제)
//                                String orderId = orderIds.get((index - 1) % orderIds.size());
//
//                                ps.setBigDecimal(1, new java.math.BigDecimal(
//                                        10000 + (index % 50000))); // amount
//                                ps.setString(2, generatePaymentStatus(index)); // status
//                                ps.setString(3, generatePaymentKey(index)); // payment_key
//                                ps.setString(4, orderId); // order_id
//                            }
//
//                            @Override
//                            public int getBatchSize() {
//                                return 2000;
//                            }
//                        });
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    private List<String> getOrderIds() {
//        String sql = "SELECT order_id FROM orders ORDER BY id";
//
//        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("order_id"));
//    }
//
//    private String generatePaymentStatus(int index) {
//        int statusRandom = index % 100;
//        if (statusRandom < 85) {
//            return "SUCCESS";
//        } else if (statusRandom < 95) {
//            return "PENDING";
//        } else {
//            return "FAILED";
//        }
//    }
//
//    private String generatePaymentKey(int index) {
//        // 실제 결제 시스템처럼 유니크한 키 생성
//        return "PAY_" + System.currentTimeMillis() + "_" + String.format("%06d", index);
//    }
//
//
//    private void initReviews50000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        // CLOSE 상태인 경매들의 ID를 가져와서 리뷰 생성
//        List<Long> closedAuctionIds = getClosedAuctionIds();
//
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//        AtomicInteger atomicInteger = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                jdbcTemplate.batchUpdate("""
//                                insert into review(
//                                                 auction_id,
//                                                 reviewer_member_id,
//                                                 seller_member_id,
//                                                 rating,
//                                                 content
//                                )
//                                values (?,?,?,?,?)
//                                """,
//                        new BatchPreparedStatementSetter() {
//                            @Override
//                            public void setValues(PreparedStatement ps, int i)
//                            throws SQLException {
//                                int index = atomicInteger.incrementAndGet();
//
//                                // CLOSE 상태 경매 중에서 랜덤하게 선택
//                                Long auctionId =
//                                        closedAuctionIds.get(index % closedAuctionIds.size());
//
//                                ps.setLong(1, auctionId); // auction_id
//                                ps.setInt(2, (index % 20) + 2); // reviewer_member_id (2~21)
//                                ps.setInt(3, 1); // seller_member_id (고정값 1)
//                                ps.setInt(4, (index % 5) + 1); // rating (1~5)
//                                ps.setString(5, generateReviewContent(index)); // content
//                            }
//
//                            @Override
//                            public int getBatchSize() {
//                                return 2500;
//                            }
//                        });
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    private List<Long> getClosedAuctionIds() {
//        String sql = "SELECT auction_id FROM auctions WHERE status = 'CLOSE'";
//
//        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("auction_id"));
//    }
//
//    private String generateReviewContent(int index) {
//        String[] reviewTemplates = {
//                "상품이 설명과 정확히 일치했습니다. 만족스러운 거래였어요!",
//                "빠른 배송과 좋은 상품 상태에 감사드립니다.",
//                "예상보다 상품 상태가 좋았습니다. 추천합니다!",
//                "판매자분이 친절하고 상품도 깨끗했어요.",
//                "가격 대비 만족스러운 상품이었습니다.",
//                "포장도 꼼꼼히 해주시고 상품도 좋았어요.",
//                "다음에도 거래하고 싶습니다. 감사합니다!",
//                "상품 설명이 정확하고 배송도 빨랐어요.",
//                "품질이 기대 이상이었습니다. 좋은 거래였어요!",
//                "신뢰할 수 있는 판매자입니다. 추천해요!"
//        };
//
//        return reviewTemplates[index % reviewTemplates.length] + " (리뷰 #" + index + ")";
//    }
//
//    // 위시리스트 초기화 메서드 (30,000개 생성)
//    // 5000개는 삭제 상태
//    private void initWishlist30000(int threadCount, ExecutorService executorService)
//            throws InterruptedException {
//        // 모든 경매 ID를 가져옴 (OPEN, CLOSE 상태만)
//        List<Long> auctionIds = getAvailableAuctionIds();
//
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//        AtomicInteger atomicInteger = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            excute(executorService, () -> {
//                jdbcTemplate.batchUpdate("""
//                                insert into wishlist(
//                                                   member_id,
//                                                   auction_id,
//                                                   deleted,
//                                                   created_at,
//                                                   updated_at)
//                                values (?,?,?,now(),now())
//                                """,
//                        new BatchPreparedStatementSetter() {
//                            @Override
//                            public void setValues(PreparedStatement ps, int i)
//                            throws SQLException {
//                                int index = atomicInteger.incrementAndGet();
//
//                                // 멤버 ID는 2~21 범위에서 선택 (기존 코드와 동일한 범위)
//                                Long memberId = (long) ((index % 20) + 2);
//
//                                // 경매 ID는 사용 가능한 경매들 중에서 랜덤하게 선택
//                                Long auctionId = auctionIds.get(index % auctionIds.size());
//
//                                // 30,000개 중 5,000개 정도를 soft delete 상태로 설정 (약 16.7%)
//                                boolean isDeleted = (index % 6) == 0; // 6개 중 1개씩 삭제 상태
//
//                                ps.setLong(1, memberId);
//                                ps.setLong(2, auctionId);
//                                ps.setBoolean(3, isDeleted);
//                            }
//
//                            @Override
//                            public int getBatchSize() {
//                                return 2500;
//                            }
//                        });
//            }, countDownLatch);
//        }
//        countDownLatch.await();
//    }
//
//    // OPEN, CLOSE 상태의 경매 ID들을 가져오는 메서드
//    private List<Long> getAvailableAuctionIds() {
//        String sql = "SELECT auction_id FROM auctions WHERE status IN ('OPEN', 'CLOSE')";
//
//        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("auction_id"));
//    }
//
//    private Queue<AuctionPair> getAuctionPairs() {
//        String sql = "SELECT auction_id, status "
//                + "FROM auctions ";
//
//        List<AuctionPair> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
//            Long auctionId = rs.getLong("auction_id");
//            String statusStr = rs.getString("status");
//            AuctionStatus status = AuctionStatus.valueOf(statusStr);
//            return new AuctionPair(auctionId, status);
//        });
//
//        return new LinkedList<>(result);
//    }
//
//    private void excute(ExecutorService executorService, Runnable runnable,
//            CountDownLatch countDownLatch) {
//        executorService.execute(new InitRunable(runnable, countDownLatch));
//    }
//
//    private record AuctionPair(Long auctionId, AuctionStatus auctionStatus) {
//
//    }
//
//    private static class InitRunable implements Runnable {
//
//        private final Runnable runnable;
//        private final CountDownLatch countDownLatch;
//
//        public InitRunable(Runnable runnable, CountDownLatch countDownLatch) {
//            this.runnable = runnable;
//            this.countDownLatch = countDownLatch;
//        }
//
//        @Override
//        public void run() {
//            try {
//                log.info("Init process started");
//                runnable.run();
//            } finally {
//                countDownLatch.countDown();
//            }
//        }
//    }
//}
