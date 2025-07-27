package com.deal4u.fourplease.domain.auth.controller;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.service.LogoutService;
import com.deal4u.fourplease.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;

    @PostMapping("/reissue/token")
    public ResponseEntity<Void> refreshAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = authService.refreshAccessToken(refreshToken);

        // access token → 헤더
        response.setHeader("Authorization", "Bearer " + tokenPair.accessToken());

        // refresh token → 쿠키
        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(false) // 운영 환경에서는 true
                .path("/")
                .sameSite("None") // 운영 환경에서는 Strict
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok().build(); // 바디 없이 OK 응답
    }

    @PostMapping("/logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 – 토큰 누락 또는 유효하지 않음"),
            @ApiResponse(responseCode = "403", description = "이미 무효화된 토큰")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        return logoutService.logout(refreshToken);
    }

    @DeleteMapping("/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> disconnectPlatforms(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @AuthenticationPrincipal Member member) {
        return authService.deactivateMember(refreshToken, member);
    }
}
