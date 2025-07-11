package com.deal4u.fourplease.domain.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
){}
