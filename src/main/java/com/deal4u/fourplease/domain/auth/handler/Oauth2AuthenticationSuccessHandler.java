package com.deal4u.fourplease.domain.auth.handler;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.model.Customoauth2User;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Oauth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final String SIGNUP_REDIRECT_URL = "api/v1/signup";
    private static final String MAIN_REDIRECT_URL = "/";
    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final MemberService memberService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("SuccessHandler 실행됨");
        Customoauth2User oauth2User = (Customoauth2User) authentication.getPrincipal();
        Member member = oauth2User.getMember();

        memberService.validateMember(member);
        log.info("멤버 이메일: {}", member.getEmail());
        log.info("멤버 상태: {}", member.getStatus());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String redirectUrl;
        String accessToken;
        String jsonResponse;

        if (member.getStatus() == Status.PENDING) {
            // 아직 닉네임 설정 안했으므로, 프론트 닉네임 설정 페이지로 redirect
            accessToken = jwtProvider.generateTokenPair(member).accessToken(); // 임시 토큰 발급
            redirectUrl = SIGNUP_REDIRECT_URL;
        } else {
            // 새로운 JWT 발급
            TokenPair tokenPair = authService.createTokenPair(member);
            accessToken = tokenPair.accessToken();
            redirectUrl = MAIN_REDIRECT_URL;

            ResponseCookie refreshCookie = ResponseCookie
                    .from("refreshToken", tokenPair.refreshToken())
                    .httpOnly(true)
                    .secure(false) // 운영 환경에서는 true
                    .path("/")
                    .sameSite("Lax") // 운영 환경에서는 Strict
                    .maxAge(Duration.ofHours(1))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        jsonResponse = String.format(
                "{\"status\":\"ok\", \"redirectUrl\":\"%s\", \"accessToken\":\"%s\"}",
                redirectUrl,
                accessToken
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
