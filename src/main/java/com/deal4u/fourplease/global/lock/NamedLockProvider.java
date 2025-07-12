package com.deal4u.fourplease.global.lock;

import java.util.concurrent.locks.Lock;
import org.springframework.stereotype.Component;

/**
 * 문자열 키를 기준으로 동시성을 제어할 수 있는 이름 기반 락(Named Lock)을 제공합니다. 클라이언트는 동일한 키에 대해 공유 락(Read) 또는 배타 락(Write)을
 * 선택적으로 획득할 수 있습니다.
 *
 * <p>이 클래스는 주로 다수의 논리적인 자원에 대해 동시 접근을 제어해야 하는 상황에서 사용됩니다.
 * 예를 들어, 데이터베이스 테이블의 특정 행(row)이나 외부 리소스를 키 기반으로 잠그는 용도에 적합합니다.
 *
 * <h3>사용 시 주의사항</h3>
 * <ul>
 *   <li>락 인스턴스는 반드시 <strong>lock() → unlock()</strong>의 쌍으로 사용해야 하며, 동일한 스레드 내에서만 호출되어야 합니다.</li>
 *   <li>반환된 {@code NamedLock} 객체는 <strong>스레드 간 공유해서는 안 됩니다.</strong>
 *       내부 상태가 스레드에 종속되어 있어, 공유 시 예측할 수 없는 오류나 경쟁 조건(race condition)이 발생할 수 있습니다.</li>
 *   <li>락 해제 시점에 내부 참조 카운트가 감소하며, 참조가 0이 되면 자동으로 해당 키에 대한 리소스가 정리됩니다.
 *       그러나 사용자의 잘못된 사용(락 누락, 이중 해제 등)은 참조 누수 혹은 예상치 못한 동작을 유발할 수 있습니다.</li>
 * </ul>
 *
 * <h3>데드락 주의</h3>
 *
 * <p>이 클래스는 복수의 키에 대해 동시에 락을 획득하는 경우를 제어하지 않습니다.
 * 다음과 같은 상황에서는 <strong>순환 대기</strong>가 발생하여 데드락에 빠질 수 있습니다:
 *
 * <pre>{@code
 * Thread A: key1에 대한 락 획득 후 key2 요청
 * Thread B: key2에 대한 락 획득 후 key1 요청
 * }</pre>
 *
 * <p>이러한 위험을 방지하려면 다음 중 하나의 전략을 사용해야 합니다:
 * <ul>
 *   <li>모든 락을 <strong>고정된 순서</strong>로 획득</li>
 *   <li>락 획득 시 <strong>타임아웃</strong> 또는 재시도 로직 도입</li>
 * </ul>
 *
 * <p>따라서 이 클래스는 동시성 제어를 단순화하기 위한 목적에는 유용하지만,
 * <strong>사용자 측에서 올바른 사용 규칙을 엄격히 지켜야만</strong> 안전하게 운용할 수 있습니다.
 *
 * @author 고지훈
 */

@Component
public class NamedLockProvider implements NameLockStore {

    private final LockManager lockManager = new LockManager();

    @Override
    public NamedLock getPassLock(String lockName) {
        return new NamedSharedLockImpl(lockName);
    }

    @Override
    public NamedLock getBottleLock(String lockName) {
        return new NamedExclusiveLockImpl(lockName);
    }

    /**
     * 이름 기반 잠금을 표현하는 추상 클래스입니다.
     *
     * <p>이 클래스의 인스턴스는 하나의 스레드에서 lock() → unlock() 흐름으로 사용하는 것을 전제로 설계되어 있으며,
     * 여러 스레드에서 동시에 같은 인스턴스를 공유하여 사용하는 것은 지원하지 않습니다. 즉, 이 객체는 멀티스레드 간 공유를 고려하여 설계되지 않았습니다.
     *
     * <p><strong>공유 금지 이유:</strong>
     * 내부 상태(`lock` 필드)가 매 lock/unlock 호출 시 갱신되기 때문에, 여러 스레드가 하나의 인스턴스를 공유하면 동기화되지 않은 상태에서 상태 충돌이
     * 발생할 수 있습니다. 예를 들어, 하나의 스레드가 unlock() 중일 때 다른 스레드가 동시에 lock()을 호출하면 예상치 못한 `lock` 객체가 생성되거나,
     * {@link IllegalStateException}이 발생할 수 있습니다.
     *
     * @author 고지훈
     */
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
            lockManager.remove(key);
        }

        protected abstract Lock getTargetLock(String key);
    }

    /**
     * 이름 기반의 공유 락입니다.
     *
     * <p>멀티스레드 환경에서 동시 접근이 가능하더라도,
     * 이 클래스의 인스턴스 자체는 여러 스레드에서 공유하여 사용해서는 안 됩니다. 자세한 제한 사항은 상위 클래스 {@link AbstractNamedLock}의 설명을
     * 참고하세요.
     *
     * @author 고지훈
     */
    private class NamedSharedLockImpl extends AbstractNamedLock {

        public NamedSharedLockImpl(String key) {
            super(key);
        }

        @Override
        protected Lock getTargetLock(String key) {
            return lockManager.getLock(key).readLock();
        }
    }

    /**
     * 이름 기반의 배타 락입니다.
     *
     * <p>이 클래스의 인스턴스는 하나의 스레드에서 단독으로 사용할 것을 전제로 하며,
     * 여러 스레드에서 공유할 경우 예상치 못한 동작이 발생할 수 있습니다. 자세한 설명은 {@link AbstractNamedLock}를 참고하세요.
     *
     * @author 고지훈
     */
    private class NamedExclusiveLockImpl extends AbstractNamedLock {

        public NamedExclusiveLockImpl(String key) {
            super(key);
        }

        @Override
        protected Lock getTargetLock(String key) {
            return lockManager.getLock(key).writeLock();
        }
    }
}
