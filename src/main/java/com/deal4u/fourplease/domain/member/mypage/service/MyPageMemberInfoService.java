package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MemberInfoResponse;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageMemberInfoService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(Member member) {

        String sellerNickname = member.getNickName();

        Integer totalReviews = reviewRepository.countBySellerMemberId(member.getMemberId());
        Double averageRating = reviewRepository.getAverageRatingBySellerMemberId(
                member.getMemberId());
        averageRating = averageRating != null ? averageRating : 0.0;

        return new MemberInfoResponse(
                totalReviews,
                averageRating,
                sellerNickname
        );
    }
}
