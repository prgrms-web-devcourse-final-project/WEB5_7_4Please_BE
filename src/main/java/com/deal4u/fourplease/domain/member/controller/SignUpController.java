package com.deal4u.fourplease.domain.member.controller;

import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.dto.SignupRequst;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signup")
@RequiredArgsConstructor
public class SignUpController {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @PostMapping("/{token}")
    public ResponseEntity<?> signUp(@PathVariable String token, @RequestBody SignupRequst request) {
        if (!jwtProvider.validateToken(token)) return ResponseEntity.status(401).build();

        String email = jwtProvider.getEmailFromToken(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        member.setNickName(request.nickName());
        member.setStatus(Status.ACTIVE);
        memberRepository.save(member);

        return ResponseEntity.status(201).build();
    }

}
