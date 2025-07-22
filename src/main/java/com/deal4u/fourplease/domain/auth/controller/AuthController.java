package com.deal4u.fourplease.domain.auth.controller;

import com.deal4u.fourplease.domain.auth.dto.RefreshRequest;
import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.service.LogoutService;
import com.deal4u.fourplease.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;

    @PostMapping
    public ResponseEntity<TokenPair> refreshAccessToken(
            @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request));
    }

    @PostMapping("/logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 – 토큰 누락 또는 유효하지 않음"),
            @ApiResponse(responseCode = "403", description = "이미 무효화된 토큰")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> logout(
            @RequestBody RefreshRequest request
    ) {
        return logoutService.logout(request);
    }

    @DeleteMapping("/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> disconnectPlatforms(
            @RequestBody RefreshRequest request,
            @AuthenticationPrincipal Member member) {
        return authService.deactivateMember(request, member);
    }
}
