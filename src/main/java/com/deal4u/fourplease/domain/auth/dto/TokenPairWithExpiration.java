package com.deal4u.fourplease.domain.auth.dto;

public record TokenPairWithExpiration(
        String accessToken,
        String refreshToken,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {

}
