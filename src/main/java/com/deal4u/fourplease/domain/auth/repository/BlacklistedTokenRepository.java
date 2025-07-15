package com.deal4u.fourplease.domain.auth.repository;

import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);
}
