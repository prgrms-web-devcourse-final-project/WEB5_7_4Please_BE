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
    void create_review_not_found_auction() {}


}