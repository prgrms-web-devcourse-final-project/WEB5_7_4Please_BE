package com.deal4u.fourplease.domain.notification.pushnotification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public record PushNotificationPageRequest(
        @Min(0)
        int page,
        @Min(0)
        @Max(20)
        int size
) {

    public Pageable toPageable() {
        Sort sort = Sort.by(Direction.DESC, "createdAt");
        return PageRequest.of(this.page, this.size, sort);
    }
}
