package com.deal4u.fourplease.global.lock;

public interface NameLockStore {

    NamedLock getPassLock(String lockName);

    NamedLock getBottleLock(String lockName);
}
