package com.deal4u.fourplease.domain.auth.controller;

import com.deal4u.fourplease.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @GetMapping("/{type}/page")
    public ResponseEntity<Void> redirectLoginPage(@PathVariable("type") String type) {
        String redirectUrl = authService.getAuthorizationUrl(type);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }
}
