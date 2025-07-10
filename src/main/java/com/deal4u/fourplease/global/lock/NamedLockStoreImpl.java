package com.deal4u.fourplease.global.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.RequiredArgsConstructor;

public class NamedLockStoreImpl implements NameLockStore {

    private final Map<String, MyKeyHolder> keyStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> counts = new HashMap<>();
    private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

    @Override
    public NamedLock getPassLock(String lockName) {
        return new NamedSharedLockImpl(lockName);
    }

    @Override
    public NamedLock getBottleLock(String lockName) {
        return new NamedExclusiveLockImpl(lockName);
    }

    private ReadWriteLock getLock(String key) {
        MyKeyHolder keyHolder = getKeyHolder(key);
        synchronized (keyHolder) {
            counts.put(key, counts.getOrDefault(key, 0) + 1);
            return locks.computeIfAbsent(key, value -> new ReentrantReadWriteLock(true));
        }
    }

    private void remove(String key) {
        MyKeyHolder keyHolder = getKeyHolder(key);
        synchronized (keyHolder) {
            counts.put(key, counts.getOrDefault(key, 0) - 1);
            if (counts.get(key) == 0) {
                counts.remove(key);
                locks.remove(key);
            }
        }
    }

    private MyKeyHolder getKeyHolder(String key) {
        return keyStore.computeIfAbsent(key, value -> new MyKeyHolder(key));
    }

    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    private static class MyKeyHolder {

        private final String key;
    }

    private abstract class AbstractNamedLock implements NamedLock {

        private final String key;
        private Lock lock;

        public AbstractNamedLock(String key) {
            this.key = key;
        }

        @Override
        public void lock() {
            if (lock == null) {
                this.lock = getTargetLock(key);
            }
            this.lock.lock();
        }

        @Override
        public void unlock() {
            if (lock == null) {
                throw new IllegalStateException();
            }
            lock.unlock();
            this.lock = null;
            remove(key);
        }

        protected abstract Lock getTargetLock(String key);
    }

    private class NamedSharedLockImpl extends AbstractNamedLock {

        public NamedSharedLockImpl(String key) {
            super(key);
        }

        @Override
        protected Lock getTargetLock(String key) {
            return getLock(key).readLock();
        }
    }

    private class NamedExclusiveLockImpl extends AbstractNamedLock {

        public NamedExclusiveLockImpl(String key) {
            super(key);
        }

        @Override
        protected Lock getTargetLock(String key) {
            return getLock(key).writeLock();
        }
    }
}
