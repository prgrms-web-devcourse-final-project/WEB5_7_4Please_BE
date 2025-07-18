package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.repository.RefreshTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
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
    private JwtProvider jwtProvider;
    private RefreshTokenRepository refreshTokenRepository;
    private BlacklistedTokenRepository blacklistedTokenRepository;

    // Authorization 헤더 검증 로직
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw ErrorCode.INVALID_AUTH_HEADER.toException();
        }

        String token = authHeader.replace("Bearer ", "");
        if (token.isBlank()) {
            throw ErrorCode.INVALID_AUTH_HEADER.toException();
        }

        return token;
    }

    @Transactional
    public ResponseEntity<Void> logout(String authHeader, Member member) {
        // Authorization 헤더에서 토큰 추출 및 검증
        String token = extractTokenFromHeader(authHeader);

        // 토큰 유효성 검사
        jwtProvider.validateOrThrow(token);

        // 이미 블랙리스트에 있는지 확인
        if (blacklistedTokenRepository.existsByToken(token)) {
            throw ErrorCode.TOKEN_ALREADY_BLACKLISTED.toException();
        }

        // 액세스 토큰을 블랙리스트에 추가
        LocalDateTime expiration = jwtProvider.getExpirationFromToken(token);
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(token)
                        .expiryDate(expiration)
                        .build()
        );
        log.info("Access token이 블랙리스트에 추가됨: {}", member.getEmail());

        // 리프레시 토큰 삭제
        refreshTokenRepository.deleteByMember(member);
        log.info("사용자 로그아웃 완료: {}", member.getEmail());
        return ResponseEntity.noContent().build();
    }
}
