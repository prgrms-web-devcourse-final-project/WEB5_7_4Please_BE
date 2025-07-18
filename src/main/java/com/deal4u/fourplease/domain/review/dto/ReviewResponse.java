package com.deal4u.fourplease.domain.review.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        String nickName,
        int rating,
        String content,
        LocalDateTime reviewTime
) {

}
