package com.deal4u.fourplease.domain.bid.service;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.global.lock.NameLockStore;
import com.deal4u.fourplease.global.lock.NamedLock;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BidMultiThreadTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private NameLockStore lockStore;

    @Test
    void bid_has_lock_test() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger(0);

        Long auctionId = 1L;
        for (int i = 0; i < 2; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    bidService.createBid(1L, new BidRequest(auctionId, 1000));
                    count.incrementAndGet();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.submit(() -> {
            NamedLock lock = lockStore.getBottleLock("auction-lock:" + auctionId);
            lock.lock();
            try {
                latch.countDown();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();

            }
        });
        executorService.shutdown();

        await().atLeast(1000, TimeUnit.MILLISECONDS)
                .atMost(1200, TimeUnit.MILLISECONDS)
                .untilAtomic(count, equalTo(2));
    }
}
