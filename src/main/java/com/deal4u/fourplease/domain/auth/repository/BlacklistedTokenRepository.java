package com.deal4u.fourplease.domain.auth.repository;

import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    Optional<BlacklistedToken> findByToken(String token);
    boolean existsByToken(String token);
}
