package com.deal4u.fourplease.domain.review.controller;

import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.review.dto.ReviewListRequest;
import com.deal4u.fourplease.domain.review.dto.ReviewRequest;
import com.deal4u.fourplease.domain.review.dto.ReviewResponse;
import com.deal4u.fourplease.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관리 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성")
    @ApiResponse(responseCode = "200", description = "리뷰 작성 성공")
    @ApiResponse(responseCode = "400", description = "유효하지 않은 입력 값")
    @ApiResponse(responseCode = "404", description = "경매 또는 주문을 찾을 수 없음")
    @ApiResponse(responseCode = "409", description = "이미 리뷰를 작성한 경매")
    @PostMapping("/auctions/{auctionId}/review")
    @ResponseStatus(HttpStatus.OK)
    public void createReview(@PathVariable Long auctionId,
            @Valid @RequestBody @ParameterObject ReviewRequest request,
            @AuthenticationPrincipal Member member) {
        // 1. 로그인한 유저 정보 취득
        // (현재는 `RequestParam`으로 처리하고 있으나 추후에 로그인 유저 정보에서 취득할 예정)

        // 2. 리뷰 작성 호출
        reviewService.createReview(request, member.getMemberId());
    }

    @Operation(summary = "판매자 리뷰 조회")
    @ApiResponse(responseCode = "200", description = "판매자 리뷰 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "허용되지 않은 정렬 기준")
    @ApiResponse(responseCode = "404", description = "판매자를 찾을 수 없음")
    @GetMapping("/reviews")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ReviewResponse> getReviews(
            @PathVariable Long memberId,
            @Valid @ModelAttribute @ParameterObject ReviewListRequest request) {

        // 1. Pageable 검증 및 변환
        Pageable pageable = request.toPageable();

        // 2. 리뷰 내역 조회 호출
        return reviewService.getReviewListFor(member.getMemberId(), pageable);
    }
}
