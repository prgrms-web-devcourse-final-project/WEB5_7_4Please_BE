package com.deal4u.fourplease.domain.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderCreateRequest(
        @NotNull(message = "가격은 필수 입력 항목입니다.")
        @DecimalMin(value = "0.01", message = "가격은 0보다 커야 합니다.")
        Long price,

        @NotNull(message = "회원 ID는 필수 입력 항목입니다.")
        Long memberId
) {
}
