package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public String getAuthorizationUrl(String type) {
        return "/oauth2/authorization/" + type.toLowerCase();
    }
}
