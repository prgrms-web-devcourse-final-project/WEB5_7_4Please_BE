package com.deal4u.fourplease.domain.member.mypage.dto;

public record MemberInfoResponse(
        Integer totalReviews,
        Double averageRating,
        String nickname
) {

}
