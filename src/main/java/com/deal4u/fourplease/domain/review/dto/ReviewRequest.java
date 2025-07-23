package com.deal4u.fourplease.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(

        @Schema(description = "리뷰를 작성할 경매의 ID", example = "1")
        @NotNull(message = "경매Id는 필수 입니다.")
        Long auctionId,

        @Schema(description = "별점 (1~5 사이의 정수)", example = "5", minimum = "1", maximum = "5")
        @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 최대 5점 이하여야 합니다.")
        int rating,

        @Schema(description = "리뷰 본문 내용", example = "상품을 잘 받았습니다! 좋은 상품입니다!")
        @NotBlank(message = "리뷰 본문은 필수 입니다.")
        String content
) {

}
