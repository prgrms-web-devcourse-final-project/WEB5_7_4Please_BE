package com.deal4u.fourplease.domain.review.controller;

import com.deal4u.fourplease.domain.review.dto.ReviewRequest;
import com.deal4u.fourplease.domain.review.entity.Review;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import com.deal4u.fourplease.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private ReviewService reviewService;

    @PostMapping("/auctions/{auctionId}/review")
    @ResponseStatus(HttpStatus.OK)
    public void createReview(@PathVariable Long auctionId,
            @Valid @RequestBody ReviewRequest request, @RequestParam("memberId") Long memberId) {
        // 1. 로그인한 유저 정보 취득
        // (현재는 `RequestParam`으로 처리하고 있으나 추후에 로그인 유저 정보에서 취득할 예정)

        // 2. 리뷰 작성 호출
        reviewService.createReview(request, memberId);

    }
}
