package com.deal4u.fourplease.domain.member.controller;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final AuthService authService;

    @PostMapping("/signup/{token}")
    public ResponseEntity<?> signUp(@PathVariable String token, @RequestBody SignupRequest request) {
        if (!jwtProvider.validateToken(token)) return ResponseEntity.status(401).build();

        String email = jwtProvider.getEmailFromToken(token);
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

}
