package com.deal4u.fourplease;

import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitProcess implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        int threadCount = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        initProduct10000(threadCount, executorService);
        initAuctions10000(threadCount, executorService);
        initBid1000000(threadCount, executorService);
        executorService.shutdown();
    }

    private void initBid1000000(int threadCount, ExecutorService executorService)
            throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        Queue<AuctionPair> auctionPairs = getAuctionPairs();
        for (int i = 0; i < 100; i++) {
            initBid10000(threadCount, executorService, auctionPairs, count);
        }
    }

    private void initBid10000(int threadCount, ExecutorService executorService,
            Queue<AuctionPair> pairs, AtomicInteger count) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            excute(executorService, () -> {
                jdbcTemplate.batchUpdate(
                        """
                                insert into bid(auction_id,bidder_member_id,price,BID_TIME,is_successful_bidder,deleted,created_at,updated_at)
                                values (?,?,?,?,?,false,now(),now())
                                """,
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                AuctionPair auctionPair = null;
                                int countValue = -1;
                                synchronized (pairs) {
                                    countValue = count.incrementAndGet();
                                    if (100 > countValue) {
                                        auctionPair = pairs.peek();
                                    } else {
                                        pairs.poll();
                                        auctionPair = pairs.peek();
                                        count.set(0);
                                        countValue = 0;
                                    }
                                }

                                if (AuctionStatus.CLOSED == auctionPair.auctionStatus) {
                                    ps.setLong(1, auctionPair.auctionId());
                                    ps.setInt(2, (countValue % 20) + 2);
                                    ps.setInt(3, countValue + 10000);
                                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                                    if (count.get() == 99) {
                                        ps.setBoolean(5, true);
                                    } else {
                                        ps.setBoolean(5, false);
                                    }
                                } else if (AuctionStatus.OPEN == auctionPair.auctionStatus) {
                                    ps.setLong(1, auctionPair.auctionId());
                                    ps.setInt(2, (countValue % 20) + 2);
                                    ps.setInt(3, countValue + 10000);
                                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                                    ps.setBoolean(5, false);
                                }else{
                                    ps.setLong(1, auctionPair.auctionId());
                                    ps.setInt(2, (countValue % 20) + 2);
                                    ps.setInt(3, countValue + 10000);
                                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                                    ps.setBoolean(5, false);
                                }
                            }

                            @Override
                            public int getBatchSize() {
                                return 2500;
                            }
                        });
            }, countDownLatch);
        }
        countDownLatch.await();
    }

    private void initAuctions10000(int threadCount, ExecutorService executorService)
            throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            excute(executorService, () -> {
                jdbcTemplate.batchUpdate("""
                        insert into auctions(PRODUCT_PRODUCT_ID,starting_price,start_time,end_time,status,DELETED,updated_at,created_at)
                        values (?,1000,now(),now(),?,false,now(),now())
                        """, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int value = atomicInteger.incrementAndGet();
                        ps.setLong(1, value + 2);
                        if (value <= 7000) {
                            ps.setString(2, "CLOSED");
                        } else if (value <= 9900) {
                            ps.setString(2, "OPEN");
                        } else {
                            ps.setString(2, "FAIL");
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return 2500;
                    }
                });
            }, countDownLatch);
        }
        countDownLatch.await();
    }

    private void initProduct10000(int threadCount, ExecutorService executorService)
            throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            excute(executorService, () -> {
                try {
                    jdbcTemplate.batchUpdate("""
                            insert into products(name,description,thumbnail_url,address,DETAIL_ADDRESS,zip_code,SELLER_MEMBER_ID,CATEGORY_CATEGORY_ID,phone,deleted,created_at,updated_at) 
                            values ('testProduct', '', '', '', '', '', 1,0,'',false,now(),now())
                            """, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i)
                                throws SQLException {
                        }

                        @Override
                        public int getBatchSize() {
                            return 2500;
                        }

                    });
                } catch (Exception throwables) {
                    throwables.printStackTrace();
                }
            }, countDownLatch);
        }
        countDownLatch.await();
    }

    private Queue<AuctionPair> getAuctionPairs() {
        String sql = "SELECT auction_id, status "
                + "FROM auctions ";

        List<AuctionPair> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
           Long auctionId = rs.getLong("auction_id");
           String statusStr = rs.getString("status");
           AuctionStatus status = AuctionStatus.valueOf(statusStr);
           return new AuctionPair(auctionId, status);
        });

        return new LinkedList<>(result);
    }

    private void excute(ExecutorService executorService, Runnable runnable,
            CountDownLatch countDownLatch) {
        executorService.execute(new InitRunable(runnable, countDownLatch));
    }

    private record AuctionPair(Long auctionId, AuctionStatus auctionStatus) {

    }

    private static class InitRunable implements Runnable {

        private final Runnable runnable;
        private final CountDownLatch countDownLatch;

        public InitRunable(Runnable runnable, CountDownLatch countDownLatch) {
            this.runnable = runnable;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                log.info("Init process started");
                runnable.run();
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
