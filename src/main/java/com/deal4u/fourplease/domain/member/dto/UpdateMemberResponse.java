package com.deal4u.fourplease.domain.member.dto;

import lombok.Builder;

@Builder
public record UpdateMemberResponse(
        String message,
        String nickName
) {
}
