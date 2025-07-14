package com.deal4u.fourplease.domain.member.service;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.repository.RefreshTokenRepository;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Transactional
    public ResponseEntity<?> signup(String token, SignupRequest request) {
        if (!jwtProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "유효하지 않은 토큰입니다."));
        }

        String email = jwtProvider.getEmailFromToken(token);
        log.info("토큰에서 추출된 이메일: {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        member.setNickName(request.nickName());
        member.setStatus(Status.ACTIVE);
        memberRepository.save(member);

        log.info("닉네임 설정된 유저: {}", member.getEmail());

        TokenPair tokenPair = authService.createTokenPair(member);
        return ResponseEntity.ok(Map.of(
                "message", "닉네임 설정 완료, 로그인 성공",
                "accessToken", tokenPair.accessToken(),
                "refreshToken", tokenPair.refreshToken(),
                "redirectUrl", "/"
        ));
    }

    public ResponseEntity<?> updateMember(Member member, String nickName) {
        member.setNickName(nickName);
        memberRepository.save(member);
        return ResponseEntity.ok("업데이트 성공");
    }

    @Transactional
    public ResponseEntity<?> logout(String authHeader, Member member) {
        String token = authHeader.replace("Bearer ", "");
        if (jwtProvider.validateToken(token)) {
            LocalDateTime expiration = jwtProvider.getExpirationFromToken(token);
            blacklistedTokenRepository.save(
                    BlacklistedToken.builder()
                            .token(token)
                            .expiryDate(expiration)
                            .build()
            );
            log.info("Access token이 블랙리스트에 추가됨: {}", member.getEmail());
        }

        refreshTokenRepository.deleteByMember(member);
        log.info("사용자 로그아웃 완료: {}", member.getEmail());
        return ResponseEntity.ok("로그아웃 완료 및 권한 변경");
    }
}
