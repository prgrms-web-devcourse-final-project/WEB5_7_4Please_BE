package com.deal4u.fourplease.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull(message = "경매Id는 필수 입니다.")
        Long auctionId,
        @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 최대 5점 이하여야 합니다.")
        int rating,
        @NotBlank(message = "리뷰 본문은 필수 입니다.")
        String content
) {

}
