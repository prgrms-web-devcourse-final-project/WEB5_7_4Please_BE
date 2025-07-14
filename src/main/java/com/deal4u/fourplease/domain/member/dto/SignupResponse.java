package com.deal4u.fourplease.domain.member.dto;

public record SingupResponse(
        String message,
        String accessToken,
        String refreshToken,
        String redirectUrl
) { }
