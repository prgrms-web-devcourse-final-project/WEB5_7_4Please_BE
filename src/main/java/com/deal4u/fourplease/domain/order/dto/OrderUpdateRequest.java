package com.deal4u.fourplease.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrderUpdateRequest(
        @NotNull(message = "주소는 필수 입력 항목입니다.")
        @Size(min = 1, max = 255, message = "주소는 1자 이상 255자 이하이어야 합니다.")
        String address,

        @NotNull(message = "상세 주소는 필수 입력 항목입니다.")
        @Size(min = 1, max = 255, message = "상세 주소는 1자 이상 255자 이하이어야 합니다.")
        String addressDetail,

        @NotNull(message = "우편번호는 필수 입력 항목입니다.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
        String zipCode,

        @NotNull(message = "전화번호는 필수 입력 항목입니다.")
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
        String phone,

        @NotNull(message = "배송 요청 사항은 필수 입력 항목입니다.")
        @Size(min = 1, max = 1000, message = "내용은 1자 이상 1000자 이하이어야 합니다.")
        String content,

        @NotNull(message = "수령인은 필수 입력 항목입니다.")
        @Size(min = 1, max = 255, message = "수령인은 1자 이상 255자 이하이어야 합니다.")
        String receiver
) {
}
