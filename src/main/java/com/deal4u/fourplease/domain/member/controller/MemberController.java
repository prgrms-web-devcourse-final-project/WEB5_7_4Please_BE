package com.deal4u.fourplease.domain.member.controller;

import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.service.MemberService;
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
    private final MemberService memberService;

    @PostMapping("/signup/{token}")
    public ResponseEntity<?> signUp(@PathVariable String token, @RequestBody SignupRequest request) {
        return memberService.signup(token, request);
    }

    @PatchMapping("/members")
    public ResponseEntity<?> updateMember(
            @AuthenticationPrincipal Member member,
            @RequestBody Map<String, Object> body
    ) {
        String nickName = body.get("nickName").toString();
        return memberService.updateMember(member, nickName);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal Member member
    ) {
        return memberService.logout(authHeader, member);
    }
}
