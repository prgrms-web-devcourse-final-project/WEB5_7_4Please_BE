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
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Oauth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final String SIGNUP_REDIRECT_URL = "/signup";
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
        log.info("멤버 이메일: " + member.getEmail());
        log.info("멤버 상태: " + member.getStatus());

        response.setStatus(HttpServletResponse.SC_OK);

        if (member.getStatus() == Status.PENDING) {
            // 아직 닉네임 설정 안했으므로, 프론트 닉네임 설정 페이지로 redirect
            String tempToken = jwtProvider.generateTokenPair(member).accessToken(); // 임시 토큰 발급
            log.info("token: " + tempToken);
            response.setHeader("X-Temp-Token", tempToken);
            response.setHeader("X-Redirect-Url", MAIN_REDIRECT_URL);
        } else {
            // 새로운 JWT 발급
            TokenPair tokenPair = authService.createTokenPair(member);
            response.setHeader("Authorization", "Bearer " + tokenPair.accessToken());
            response.setHeader("X-Refresh-Token", tokenPair.refreshToken());
            response.setHeader("X-Redirect-Url", SIGNUP_REDIRECT_URL);
        }

        response.getWriter().write("{\"status\":\"ok\"}");
        response.getWriter().flush();

    }
}
