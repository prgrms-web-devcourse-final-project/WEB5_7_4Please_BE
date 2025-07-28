package com.deal4u.fourplease.domain.bid.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BidListRequest(
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다.")
        @Max(value = 100, message = "최대 사이즈는 10입니다.")
        @Parameter(description = "페이지 번호는 0부터 시작됩니다. 생략 시에는 기본값으로 0이 들어값니다.",
                example = "0",
                schema = @Schema(type = "integer", defaultValue = "0"))
        Integer page,

        @Parameter(description = "한 페이지에 표시할 리뷰 수. (최소 0, 최대 100). 생략 시 기본값으로 20이 들어갑니다.",
                example = "20",
                schema = @Schema(type = "integer",
                        minimum = "0", maximum = "100", defaultValue = "20"))
        @Min(value = 0, message = "사이즈는 0 이상이어야 합니다.")
        @Max(value = 100, message = "최대 사이즈는 100입니다.")
        Integer size
) {

    public BidListRequest {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 20;
        }
    }
}
