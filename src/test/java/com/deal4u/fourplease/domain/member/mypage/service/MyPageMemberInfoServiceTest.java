package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MemberInfoResponse;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MyPageMemberInfoServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private MyPageMemberInfoService myPageMemberInfoService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = createTestMember(1L, "testNickname");
    }

    @Test
    @DisplayName("회원 정보 조회 - 리뷰가 있는 경우")
    void getMemberInfo_WithReviews_Success() {
        // given
        Long memberId = testMember.getMemberId();
        Integer expectedTotalReviews = 5;
        Double expectedAverageRating = 4.5;

        given(reviewRepository.countBySellerMemberId(memberId))
                .willReturn(expectedTotalReviews);
        given(reviewRepository.getAverageRatingBySellerMemberId(memberId))
                .willReturn(expectedAverageRating);

        // when
        MemberInfoResponse response = myPageMemberInfoService.getMemberInfo(testMember);

        // then
        assertThat(response.totalReviews()).isEqualTo(expectedTotalReviews);
        assertThat(response.averageRating()).isEqualTo(expectedAverageRating);
        assertThat(response.nickname()).isEqualTo("testNickname");

        verify(reviewRepository).countBySellerMemberId(memberId);
        verify(reviewRepository).getAverageRatingBySellerMemberId(memberId);
    }

    @Test
    @DisplayName("회원 정보 조회 - 리뷰가 없는 경우")
    void getMemberInfo_WithoutReviews_Success() {
        // given
        Long memberId = testMember.getMemberId();
        Integer expectedTotalReviews = 0;
        Double expectedAverageRating = null;

        given(reviewRepository.countBySellerMemberId(memberId))
                .willReturn(expectedTotalReviews);
        given(reviewRepository.getAverageRatingBySellerMemberId(memberId))
                .willReturn(expectedAverageRating);

        // when
        MemberInfoResponse response = myPageMemberInfoService.getMemberInfo(testMember);

        // then
        assertThat(response.totalReviews()).isEqualTo(expectedTotalReviews);
        assertThat(response.averageRating()).isEqualTo(0.0);
        assertThat(response.nickname()).isEqualTo("testNickname");

        verify(reviewRepository).countBySellerMemberId(memberId);
        verify(reviewRepository).getAverageRatingBySellerMemberId(memberId);
    }

    private Member createTestMember(Long memberId, String nickname) {
        return Member.builder()
                .memberId(memberId)
                .nickName(nickname)
                .build();
    }
}
