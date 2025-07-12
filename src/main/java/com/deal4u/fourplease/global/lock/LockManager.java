package com.deal4u.fourplease.global.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.RequiredArgsConstructor;

public class LockManager {

    private final Map<String, MyKeyHolder> keyStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> counts = new ConcurrentHashMap<>();
    private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();


    ReadWriteLock getLock(String key) {
        MyKeyHolder keyHolder = getKeyHolder(key);
        synchronized (keyHolder) {
            return incrementRefCountOrCreate(key);
        }
    }

    void remove(String key) {
        MyKeyHolder keyHolder = getKeyHolder(key);
        synchronized (keyHolder) {
            decrementRefCountOrRemove(key);
        }
    }

    private MyKeyHolder getKeyHolder(String key) {
        return keyStore.computeIfAbsent(key, value -> new MyKeyHolder(key));
    }

    private ReadWriteLock incrementRefCountOrCreate(String key) {
        counts.put(key, counts.getOrDefault(key, 0) + 1);
        return locks.computeIfAbsent(key, value -> new ReentrantReadWriteLock(true));
    }

    private void decrementRefCountOrRemove(String key) {
        Integer count = counts.get(key);
        if (count == null || count <= 1) {
            counts.remove(key);
            locks.remove(key);
            keyStore.remove(key);
            return;
        }
        counts.put(key, count - 1);
    }

    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    private static class MyKeyHolder {

        private final String key;
    }
}
