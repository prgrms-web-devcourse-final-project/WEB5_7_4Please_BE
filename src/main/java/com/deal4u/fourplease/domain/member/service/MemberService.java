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
import com.deal4u.fourplease.global.exception.ErrorCode;
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

    private void validateNickName(String nickName) {
        if (nickName == null || nickName.isBlank()) {
            throw ErrorCode.INVALID_NICKNAME.toException();
        }

        // 길이 제한 (예: 2-20자)
        if (nickName.length() < 2 || nickName.length() > 20) {
            throw ErrorCode.NICKNAME_LENGTH_INVALID.toException();
        }

        // 특수문자 제한 (한글, 영문, 숫자, 일부 특수문자만 허용)
        if (!nickName.matches("^[가-힣a-zA-Z0-9_-]+$")) {
            throw ErrorCode.INVALID_NICKNAME_FORMAT.toException();
        }

        // 중복 체크
        if (memberRepository.existsByNickName(nickName)) {
            throw ErrorCode.NICKNAME_ALREADY_EXISTS.toException();
        }
    }

    // Authorization 헤더 검증 로직
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw ErrorCode.INVALID_AUTH_HEADER.toException();
        }

        String token = authHeader.replace("Bearer ", "");
        if (token.isBlank()) {
            throw ErrorCode.INVALID_TOKEN.toException();
        }

        return token;
    }
    @Transactional
    public ResponseEntity<?> signup(String token, SignupRequest request) {
        // 토큰 유효성 검사
        if (!jwtProvider.validateToken(token)) {
            throw ErrorCode.INVALID_TOKEN.toException();
        }

        // 닉네임 유효성 검사
        validateNickName(request.nickName());

        String email = jwtProvider.getEmailFromToken(token);
        log.info("토큰에서 추출된 이메일: {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> ErrorCode.MEMBER_NOT_FOUND.toException());

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
        // 닉네임 유효성 검사 (중복 제거)
        validateNickName(nickName);

        member.setNickName(nickName);
        memberRepository.save(member);
        return ResponseEntity.ok("업데이트 성공");
    }

    @Transactional
    public ResponseEntity<?> logout(String authHeader, Member member) {
        // Authorization 헤더에서 토큰 추출 및 검증
        String token = extractTokenFromHeader(authHeader);

        // 토큰 유효성 검사
        if (!jwtProvider.validateToken(token)) {
            throw ErrorCode.INVALID_TOKEN.toException();
        }

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
        return ResponseEntity.ok("로그아웃 완료 및 권한 변경");
    }
}
