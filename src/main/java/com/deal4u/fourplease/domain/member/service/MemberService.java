package com.deal4u.fourplease.domain.member.service;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.dto.SignupResponse;
import com.deal4u.fourplease.domain.member.dto.UpdateMemberResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private static final String MAIN_REDIRECT_URL = "/";
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final AuthService authService;

    public void validateMember(Member member) {
        if (member == null) {
            throw ErrorCode.MEMBER_NOT_FOUND.toException();
        }

        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            throw ErrorCode.OAUTH_EMAIL_NOT_FOUND.toException();
        }

        if (member.getStatus() == Status.DELETED) {
            throw ErrorCode.MEMBER_WITHDRAWN.toException();
        }
    }

    public void validateNickName(String nickName) {
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

    @Transactional
    public ResponseEntity<SignupResponse> signup(String token, SignupRequest request) {
        // 토큰 유효성 검사
        jwtProvider.validateOrThrow(token);

        String email = jwtProvider.getEmailFromToken(token);
        log.info("토큰에서 추출된 이메일: {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);

        validateMember(member);

        validateNickName(request.nickName());

        member.setNickName(request.nickName());
        member.setStatus(Status.ACTIVE);
        memberRepository.save(member);

        log.info("설정된 닉네임: {}", member.getNickName());

        TokenPair tokenPair = authService.createTokenPair(member);
        SignupResponse response = SignupResponse.builder()
                .message("닉네임 설정 완료, 로그인 성공")
                .accessToken(tokenPair.accessToken())
                .redirectUrl(MAIN_REDIRECT_URL)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(false) // 운영 환경에서는 true
                .path("/")
                .sameSite("None") // 운영 환경에서는 Strict
                .maxAge(Duration.ofHours(1))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).header(
                HttpHeaders.SET_COOKIE, refreshCookie.toString()
        ).body(response);
    }

    public ResponseEntity<UpdateMemberResponse> updateMember(Member member, String nickName) {
        validateNickName(nickName);
        member.setNickName(nickName);
        memberRepository.save(member);
        UpdateMemberResponse response = UpdateMemberResponse.builder()
                .message("업데이트 성공")
                .nickName(nickName)
                .build();

        return ResponseEntity.ok(response);
    }


}
