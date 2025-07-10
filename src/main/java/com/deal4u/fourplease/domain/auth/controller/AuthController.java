package com.deal4u.fourplease.domain.auth.controller;

import com.deal4u.fourplease.domain.auth.client.GoogleOAuthClient;
import com.deal4u.fourplease.domain.auth.dto.LoginCodeRequest;
import com.deal4u.fourplease.domain.auth.service.AuthLoginService;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthLoginService authLoginService;
    private final GoogleOAuthClient googleOAuthClient;

    @GetMapping("/{type}/page")
    public ResponseEntity<Void> redirectLoginPage(@PathVariable("type") String type) {
        String redirectUrl = authService.getAuthorizationUrl(type);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }

    @PostMapping("/{type}")
    public ResponseEntity<?> login(@PathVariable("type") String type, @RequestBody LoginCodeRequest request) {
        return authLoginService.login(type, request.code());
    }

}
