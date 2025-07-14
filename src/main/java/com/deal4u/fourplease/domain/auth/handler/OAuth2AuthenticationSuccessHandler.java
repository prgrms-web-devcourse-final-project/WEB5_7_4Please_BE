package com.deal4u.fourplease.domain.auth.handler;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.model.CustomOAuth2User;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("SuccessHandler 실행됨");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Member member = oAuth2User.getMember();
        log.info("멤버의 이메일: "+member.getEmail());
        log.info("멤버의 상태: "+member.getStatus());

        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> responseBody = new HashMap<>();

        if (member.getStatus() == Status.PENDING) {
            // 아직 닉네임 설정 안했으므로, 프론트 닉네임 설정 페이지로 redirect
            String token = jwtProvider.generateTokenPair(member).accessToken(); // 혹은 따로 generate 임시 토큰
            log.info("token: "+token);
            responseBody.put("message", "닉네임 설정이 필요합니다.");
            responseBody.put("token", token);
            responseBody.put("redirectUrl", "/signup"); // 회원가입
        } else {
            // 새로운 JWT 발급
            TokenPair tokenPair = authService.createTokenPair(member);

            responseBody.put("message", "로그인 성공");
            responseBody.put("accessToken", tokenPair.accessToken());
            responseBody.put("refreshToken", tokenPair.refreshToken());
            responseBody.put("redirectUrl", "/"); // 메인 페이지로
        }

        // JSON 응답 전송
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
        response.getWriter().flush();

    }
}
