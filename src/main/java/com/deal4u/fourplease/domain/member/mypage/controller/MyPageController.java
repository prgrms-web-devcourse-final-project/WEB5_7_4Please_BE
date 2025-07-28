package com.deal4u.fourplease.domain.member.mypage.controller;

import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.mypage.dto.MemberInfoResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageAuctionHistory;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import com.deal4u.fourplease.domain.member.mypage.service.MyPageAuctionHistoryService;
import com.deal4u.fourplease.domain.member.mypage.service.MyPageBidHistoryService;
import com.deal4u.fourplease.domain.member.mypage.service.MyPageMemberInfoService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/my")
public class MyPageController {

    private final MyPageBidHistoryService myPageBidHistoryService;
    private final MyPageAuctionHistoryService myPageAuctionHistoryService;
    private final MyPageMemberInfoService myPageMemberInfoService;


    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마이페이지 입찰 내역 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/bids")
    public PageResponse<MyPageBidHistory> getMyBidHistory(
            @AuthenticationPrincipal Member member,
            @PageableDefault Pageable pageable) {
        return myPageBidHistoryService.getMyBidHistory(member, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마이페이지 판매 내역 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/auctions")
    public PageResponse<MyPageAuctionHistory> getMyAuctionHistory(
            @AuthenticationPrincipal Member member,
            @PageableDefault Pageable pageable) {
        return myPageAuctionHistoryService.getMyAuctionHistory(member, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마이페이지 내 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/member")
    public MemberInfoResponse getMemberInfo(
            @AuthenticationPrincipal Member member
    ) {
        return myPageMemberInfoService.getMemberInfo(member);
    }
}
