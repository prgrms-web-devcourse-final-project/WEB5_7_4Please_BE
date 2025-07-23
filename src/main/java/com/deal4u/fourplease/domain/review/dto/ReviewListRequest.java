package com.deal4u.fourplease.domain.review.dto;

import com.deal4u.fourplease.global.exception.ErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ReviewListRequest(

        @Min(value = 0, message = "페이지는 0 이상이어야 합니다.")
        Integer page,
        @Min(value = 3, message = "사이즈는 3 이상이어야 합니다.")
        @Max(value = 10, message = "최대 사이즈는 10입니다.")
        Integer size,
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
