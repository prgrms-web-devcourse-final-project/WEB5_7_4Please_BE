package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.entity.RefreshToken;
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
public class AuthService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // 로그인 시 토큰 생성 및 저장
    public TokenPair createTokenPair(Member member) {


        TokenPair tokenPair = jwtProvider.generateTokenPair(member);

        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);
        refreshTokenRepository.findByMember(member)
                .ifPresentOrElse(
                        existing -> existing.updateToken(tokenPair.refreshToken(), expiryDate),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .member(member)
                                        .token(tokenPair.refreshToken())
                                        .expiryDate(expiryDate)
                                        .build()
                        )
                );
        return tokenPair;
    }

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

    // 리프레시 토큰을 통해 새로운 액세스토큰과 리프레시 토큰을 재발급
    public TokenPair refreshAccessToken(String refreshToken) {
        // 토큰 유효성 검사
        jwtProvider.validateOrThrow(refreshToken);

        // 토큰 타입 확인 (refresh인지 확인)
        String tokenType = jwtProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw ErrorCode.INVALID_TOKEN_TYPE.toException();
        }

        // DB에 저장된 토큰인지 확인
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(ErrorCode.INVALID_REFRESH_TOKEN::toException);

        // DB 기준으로 만료 여부 확인
        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken); // 만료된 토큰 삭제
            throw ErrorCode.TOKEN_EXPIRED.toException();
        }
        // 검증된 토큰을 가진 유저이므로 새로운 토큰 생성
        Member member = savedToken.getMember();
        return createTokenPair(member);
    }


}
