package com.deal4u.fourplease.global.lock;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class NamedLockStoreTest {

    private NamedLockStoreImpl namedLockStore;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        namedLockStore = new NamedLockStoreImpl();
        executorService = Executors.newFixedThreadPool(20);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Nested
    @DisplayName("Exclusive Lock")
    class ExclusiveLockTests {

        @Test
        void shouldAllowOnlyOneThreadAtOnce() {
            String lockName = "exclusive";
            AtomicInteger counter = new AtomicInteger();
            AtomicInteger maxConcurrent = new AtomicInteger();
            AtomicInteger current = new AtomicInteger();

            for (int i = 0; i < 10; i++) {
                executorService.submit(() -> {
                    NamedLock lock = namedLockStore.getBottleLock(lockName);
                    lock.lock();
                    try {
                        int now = current.incrementAndGet();
                        maxConcurrent.updateAndGet(m -> Math.max(m, now));
                        counter.incrementAndGet();
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        current.decrementAndGet();
                        lock.unlock();
                    }
                });
            }

            await().dontCatchUncaughtExceptions()
                    .atMost(500, TimeUnit.MILLISECONDS)
                    .untilAtomic(counter, equalTo(10));
            assertEquals(1, maxConcurrent.get());
        }

        @Test
        void shouldThrowWhenUnlockWithoutLock() {
            NamedLock lock = namedLockStore.getBottleLock("illegal");
            assertThrows(IllegalStateException.class, lock::unlock);
        }
    }

    @Nested
    @DisplayName("Shared Lock")
    class SharedLockTests {

        @Test
        void shouldAllowConcurrentReads() {
            String lockName = "shared";
            AtomicInteger current = new AtomicInteger();
            AtomicInteger max = new AtomicInteger();
            AtomicInteger finished = new AtomicInteger();

            for (int i = 0; i < 10; i++) {
                executorService.submit(() -> {
                    NamedLock lock = namedLockStore.getPassLock(lockName);
                    lock.lock();
                    try {
                        int now = current.incrementAndGet();
                        max.updateAndGet(m -> Math.max(m, now));
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        current.decrementAndGet();
                        finished.incrementAndGet();
                        lock.unlock();
                    }
                });
            }

            await().atMost(150, TimeUnit.MILLISECONDS).untilAtomic(finished, equalTo(10));
            assertTrue(max.get() > 1);
        }
    }

    @Nested
    @DisplayName("Read-Write Interaction")
    class ReadWriteLockTests {


        @RepeatedTest(10)
        void shouldBlockReadersWhenWriterActive() {
            String lockName = "rw";
            AtomicInteger readers = new AtomicInteger();
            AtomicInteger writers = new AtomicInteger();
            List<String> log = Collections.synchronizedList(new ArrayList<>());

            int readerCount = 10;
            for (int i = 0; i < readerCount; i++) {
                int id = i;
                executorService.submit(() -> {
                    NamedLock lock = namedLockStore.getPassLock(lockName);
                    lock.lock();
                    try {
                        log.add("R" + id + "-start");
                        readers.incrementAndGet();
                    } finally {
                        readers.decrementAndGet();
                        log.add("R" + id + "-end");
                        lock.unlock();
                    }
                });
            }

            int writerCount = 5;
            for (int i = 0; i < writerCount; i++) {
                int id = i;
                executorService.submit(() -> {
                    NamedLock lock = namedLockStore.getBottleLock(lockName);
                    lock.lock();
                    try {
                        log.add("W" + id + "-start");
                        assertEquals(0, readers.get());
                        writers.incrementAndGet();
                        log.add("W" + id + "-end");
                    } finally {
                        lock.unlock();
                    }
                });
            }

            await().atMost(readerCount, TimeUnit.SECONDS)
                    .untilAtomic(writers, equalTo(writerCount));
            long readerEndCount = log.stream().filter(s -> s.startsWith("R") && s.endsWith("end"))
                    .count();
            assertEquals(readerCount, readerEndCount);
        }
    }

    @Nested
    @DisplayName("Multiple Keys")
    class MultipleKeyTests {

        @Test
        void shouldHandleLocksIndependentlyPerKey() {
            int keyCount = 5;
            int threadsPerKey = 4;
            Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();
            AtomicInteger finished = new AtomicInteger();

            for (int i = 0; i < keyCount; i++) {
                String key = "key-" + i;
                counts.put(key, new AtomicInteger());

                for (int j = 0; j < threadsPerKey; j++) {
                    executorService.submit(() -> {
                        NamedLock lock = namedLockStore.getBottleLock(key);
                        lock.lock();
                        try {
                            counts.get(key).incrementAndGet();
                        } finally {
                            lock.unlock();
                            finished.incrementAndGet();
                        }
                    });
                }
            }

            await().atMost(5, TimeUnit.SECONDS)
                    .untilAtomic(finished, equalTo(keyCount * threadsPerKey));
            for (int i = 0; i < keyCount; i++) {
                assertEquals(threadsPerKey, counts.get("key-" + i).get());
            }
        }
    }

    @Nested
    @DisplayName("Lock Reuse")
    class LockReuseTests {

        @Test
        void shouldPreserveSequentialConsistency() {
            String lockName = "reuse";
            int threadCount = 100;
            CountDownLatch done = new CountDownLatch(threadCount);
            LinkedList<Integer> result = new LinkedList<>();
            NonAtomicCount counter = new NonAtomicCount();

            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    NamedLock lock = namedLockStore.getBottleLock(lockName);
                    lock.lock();
                    try {
                        counter.increment();
                        result.add(counter.getCount());
                    } finally {
                        lock.unlock();
                        done.countDown();
                    }
                });
            }

            await().atMost(5, TimeUnit.SECONDS).until(() -> done.getCount() == 0);
            List<Integer> expected = new ArrayList<>();
            for (int i = 1; i <= threadCount; i++) {
                expected.add(i);
            }
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("Stress")
    class StressTests {

        @RepeatedTest(5)
        void shouldHandleHighConcurrencyWithoutFailure() {
            int threadCount = 50;
            int opsPerThread = 20;
            AtomicInteger totalOps = new AtomicInteger();
            AtomicReference<Exception> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                int tid = i;
                executorService.submit(() -> {
                    try {
                        for (int j = 0; j < opsPerThread; j++) {
                            String key = "stress-" + (tid % 10);
                            NamedLock lock = (tid + j) % 3 != 0
                                    ? namedLockStore.getPassLock(key)
                                    : namedLockStore.getBottleLock(key);
                            lock.lock();
                            try {
                                totalOps.incrementAndGet();
                            } finally {
                                lock.unlock();
                            }
                        }
                    } catch (Exception e) {
                        error.set(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            await().atMost(10, TimeUnit.SECONDS).until(() -> done.getCount() == 0);
            assertNull(error.get());
            assertEquals(threadCount * opsPerThread, totalOps.get());
        }
    }

    @Getter
    private static class NonAtomicCount {

        private int count = 0;

        public void increment() {
            count++;
        }
    }
}