package com.deal4u.fourplease.domain.review.dto;

import com.deal4u.fourplease.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ReviewListRequest(

        @Min(value = 0, message = "페이지는 0 이상이어야 합니다.")
        @Parameter(description = "페이지 번호는 0부터 시작됩니다. 생략 시에는 기본값으로 0이 들어값니다.",
                example = "0",
                schema = @Schema(type = "integer", defaultValue = "0"))
        Integer page,

        @Parameter(description = "한 페이지에 표시할 리뷰 수. (최소 3, 최대 10). 생략 시 기본값으로 3이 들어갑니다.",
                example = "3",
                schema = @Schema(type = "integer",
                        minimum = "3", maximum = "10", defaultValue = "3"))
        @Min(value = 3, message = "사이즈는 3 이상이어야 합니다.")
        @Max(value = 10, message = "최대 사이즈는 10입니다.")
        Integer size,

        @Parameter(description = "정렬 기준. 형식: '정렬할 속성,정렬 방식'."
                + "<br>현재 'createdAt'(작성일시) 속성만 지원합니다."
                + "<br>정렬 방식: 'asc'(오름차순), 'desc'(내림차순)."
                + "<br>생략 시 기본값 'createdAt,desc'.",
                example = "createdAt,desc",
                schema = @Schema(type = "string", allowableValues = {"createdAt,desc",
                        "createdAt,asc"}, defaultValue = "createdAt,desc"))
        String sort
) {

    public ReviewListRequest {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 3;
        }

        if (sort == null || sort.isBlank()) {
            sort = "createdAt,desc";
        }

        String sortProperty = sort.split(",")[0];
        if (!"createdAt".equals(sortProperty)) {
            throw ErrorCode.INVALID_REVIEW_SORT.toException();
        }
    }

    public Pageable toPageable() {
        String[] sortParams = this.sort.split(",");
        String property = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(this.page, this.size, Sort.by(direction, property));
    }
}
