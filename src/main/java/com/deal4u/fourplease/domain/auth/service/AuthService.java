package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.RefreshToken;
import com.deal4u.fourplease.domain.auth.repository.RefreshTokenRepository;
//import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

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

    //토큰 재발급
    public TokenPair refreshAccessToken(String refreshToken) {
        // 1. 토큰 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. 토큰 타입 확인 (refresh인지 확인)
        if (!"refresh".equals(jwtProvider.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        // 3. DB에 저장된 토큰인지 확인
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리프레시 토큰입니다."));

        // 4. DB 기준으로 만료 여부 확인
        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken); // 만료된 토큰 삭제
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }
        // 검증된 토큰을 가진 유저이므로 새로운 토큰 생성
        Member member = savedToken.getMember();
        return createTokenPair(member);
    }


}
