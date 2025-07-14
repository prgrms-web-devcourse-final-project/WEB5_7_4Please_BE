package com.deal4u.fourplease.domain.member.controller;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.repository.RefreshTokenRepository;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/signup/{token}")
    public ResponseEntity<?> signUp(
            @PathVariable String token,
            @RequestBody SignupRequest request
    ) {
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
        log.info("닉네임 설정된 유저: " + member.getEmail());
        log.info("닉네임: " + request.nickName());

        TokenPair tokenPair = authService.createTokenPair(member);
        return ResponseEntity.ok(Map.of(
                "message", "닉네임 설정 완료, 로그인 성공",
                "accessToken", tokenPair.accessToken(),
                "refreshToken", tokenPair.refreshToken(),
                "redirectUrl", "/"
        ));
    }

    @PatchMapping("/members")
    public ResponseEntity<?> updateMember(
            @AuthenticationPrincipal Member member,
            @RequestBody Map<String, Object> body
    ) {
        String nickName = body.get("nickName").toString();
        member.setNickName(nickName);
        memberRepository.save(member);
        return ResponseEntity.ok("업데이트 성공");
    }

    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal Member member
    ) {
            String token = authHeader.replace("Bearer ", "");
            if (jwtProvider.validateToken(token)) {
                LocalDateTime expiration = jwtProvider.getExpirationFromToken(token);
                blacklistedTokenRepository.save(
                        BlacklistedToken.builder()
                            .token(token)
                            .expiryDate(expiration)
                            .build()
                );
                log.info("Access token이 블랙리스트에 추가되었습니다: {}", member.getEmail());
            }
        refreshTokenRepository.deleteByMember(member);
        log.info("사용자 로그아웃 완료: {}", member.getEmail());

        return ResponseEntity.ok("로그아웃 완료 및 권한 변경");
    }



}
