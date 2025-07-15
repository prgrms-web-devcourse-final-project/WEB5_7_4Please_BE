package com.deal4u.fourplease.domain.review.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Test
    @DisplayName("신규 리뷰 작성 성공")
    void create_review_success() {

    }

    @Test
    @DisplayName("신규 리뷰 작성 실패 (경매 조회 실패)")
    void create_review_not_found_auction() {
    }

    @Test
    @DisplayName("신규 리뷰 작성 실패 (종료되지 않은 경매)")
    void create_review_not_closed_auction() {
    }

    @Test
    @DisplayName("신규 리뷰 작성 실패 (존재하지 않는 입찰자(유저))")
    void create_review_not_found_member() {
    }

    @Test
    @DisplayName("신규 리뷰 작성 실패 (이미 작성된 리뷰가 존재)")
    void create_review_already_exists_review() {}

    @Test
    @DisplayName("신규 리뷰 작성 실패 (입찰이 존재하지 않는 경우)")
    void create_review_not_found_bid() {}
}