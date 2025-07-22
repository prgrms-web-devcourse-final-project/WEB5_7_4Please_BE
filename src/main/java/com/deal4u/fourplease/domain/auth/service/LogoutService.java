package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.dto.RefreshRequest;
import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {
    private final JwtProvider jwtProvider;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Transactional
    public ResponseEntity<Void> logout(String refreshToken) {
        // 1. 토큰 유효성 검사
        jwtProvider.validateOrThrow(refreshToken);

        // 2. 토큰 타입이 Refresh인지 확인
        String tokenType = jwtProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw ErrorCode.INVALID_TOKEN_TYPE.toException();
        }

        // 3. 이미 블랙리스트에 있는지 확인
        if (blacklistedTokenRepository.existsByToken(refreshToken)) {
            throw ErrorCode.TOKEN_ALREADY_BLACKLISTED.toException();
        }

        // 4. 토큰의 만료일 추출
        LocalDateTime expiration = jwtProvider.getExpirationFromToken(refreshToken);

        // 5. 리프레시 토큰을 블랙리스트에 추가
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(refreshToken)
                        .expiryDate(expiration)
                        .build()
        );
        log.info("Refresh token이 블랙리스트에 추가됨");

        return ResponseEntity.noContent().build();
    }
}
