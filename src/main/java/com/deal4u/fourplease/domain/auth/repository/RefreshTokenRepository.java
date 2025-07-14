package com.deal4u.fourplease.domain.auth.repository;

import com.deal4u.fourplease.domain.auth.entity.RefreshToken;
import com.deal4u.fourplease.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);

    Optional<RefreshToken> findByToken(String token);


    // 만료된 refreshToken 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    void deleteByMember(Member member);
}
