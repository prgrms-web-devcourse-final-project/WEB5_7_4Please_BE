package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.RefreshToken;
import com.deal4u.fourplease.domain.auth.repository.RefreshTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.service.MemberService;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    //리프레시 토큰을 통해 새로운 액세스토큰과 리프레시 토큰을 재발급
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
                .orElseThrow(() -> ErrorCode.INVALID_REFRESH_TOKEN.toException());

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
