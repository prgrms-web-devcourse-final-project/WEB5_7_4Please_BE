package com.deal4u.fourplease.domain.auth.service;


import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtProvider jwtProvider;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final MemberRepository memberRepository;
    private final LogoutService logoutService;

    // 로그인 시 토큰 생성
    public TokenPair createTokenPair(Member member) {
        return jwtProvider.generateTokenPair(member);
    }

    // 리프레시 토큰을 통해 새로운 토큰을 재발급
    public TokenPair refreshAccessToken(String refreshToken) {
        // 토큰 유효성 검사
        jwtProvider.validateOrThrow(refreshToken);

        // 토큰 타입 확인 (refresh인지 확인)
        String tokenType = jwtProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw ErrorCode.INVALID_TOKEN_TYPE.toException();
        }

        if (blacklistedTokenRepository.existsByToken(refreshToken)) {
            throw ErrorCode.TOKEN_ALREADY_BLACKLISTED.toException();
        }

        // 4. 토큰에서 사용자 이메일 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);

        LocalDateTime expiration = jwtProvider.getExpirationFromToken(refreshToken);

        // 기존에 있던 리프레시 토큰을 블랙리스트에 등록
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(refreshToken)
                        .expiryDate(expiration)
                        .build()
        );

        // 5. 새 토큰 발급 (Access + Refresh 둘 다)
        return createTokenPair(member);
    }

    public ResponseEntity<Void> deactivateMember(String refreshToken, Member member) {
        logoutService.logout(refreshToken);

        member.setStatus(Status.DELETED);
        memberRepository.save(member);

        log.info("회원 탈퇴 완료: {}", member.getEmail());
        return ResponseEntity.noContent().build();
    }
}
