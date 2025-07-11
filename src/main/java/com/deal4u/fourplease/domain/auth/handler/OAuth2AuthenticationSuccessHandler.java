package com.deal4u.fourplease.domain.auth.handler;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.model.CustomOAuth2User;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("SuccessHandler 실행됨");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Member member = oAuth2User.getMember();

        if (member.getStatus() == Status.PENDING) {
            // 아직 닉네임 설정 안했으므로, 프론트 닉네임 설정 페이지로 redirect
            String token = jwtProvider.generateTokenPair(member).accessToken(); // 혹은 따로 generate 임시 토큰
            log.info("닉네임 설정에 진입");
            response.sendRedirect("http://localhost:3000/signup?token=" + token); // 프론트 닉네임 설정 페이지
            return;
        }

        // JWT 발급
        TokenPair tokenPair = authService.createTokenPair(member);

        // JWT를 HTTP 응답 헤더에 담아 전달
        response.setHeader("Authorization", "Bearer " + tokenPair.accessToken());
        response.setHeader("Refresh-Token", tokenPair.refreshToken());


    }
}
