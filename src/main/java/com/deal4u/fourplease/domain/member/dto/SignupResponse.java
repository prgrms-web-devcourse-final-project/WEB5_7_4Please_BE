package com.deal4u.fourplease.domain.member.dto;

import lombok.Builder;

@Builder
public record SignupResponse(
        String message,
        String accessToken,
        String refreshToken,
        String redirectUrl
) {
}
