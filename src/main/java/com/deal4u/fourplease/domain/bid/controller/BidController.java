package com.deal4u.fourplease.domain.bid.controller;

import com.deal4u.fourplease.domain.bid.dto.BidListRequest;
import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.review.dto.ReviewListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Tag(name = "Bid", description = "입찰 관리 API")
public class BidController {

    private final BidService bidService;

    /**
     * 특정 경매에 대한 입찰 목록을 페이징하여 조회합니다. 정렬 순서는 서비스 로직 상에서 고정하였습니다. (bidPrice DESC, bidTime ASC)
     *
     * @param auctionId 경매 ID
     * @param request  `Client`에서 요청한 Paging 정보 (e.g., ?page=0&size=10) 상기의 내용의 경우, `Page`와
     *                  `Slice`중 어느쪽으로 할지 협의가 필요한 부분입니다. `totalElements`를 화면 상에서 필요로 하기 때문에, `Page`로
     *                  우선 구현하였습니다.
     * @return Paging 처리 된 입찰 목록
     */
    @Operation(summary = "입찰 내역 조회")
    @ApiResponse(responseCode = "200", description = "해당 경매의 입찰 내역 조회 성공")
    @ApiResponse(responseCode = "404", description = "경매를 찾을 수 없음")
    @GetMapping("/auctions/{auctionId}/bids")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<BidResponse> getBids(@PathVariable(name = "auctionId") Long auctionId,
            @Valid @ModelAttribute @ParameterObject BidListRequest request) {

        // 0. BidListRequest -> Pageable로 변환
        Pageable pageable = PageRequest.of(request.page(), request.size());

        // 1. 일찰 내역 조회 호출
        return bidService.getBidListForAuction(auctionId, pageable);
    }

    @Operation(summary = "입찰 추가")
    @ApiResponse(responseCode = "201", description = "해당 경매에 대해 입찰을 성공")
    @ApiResponse(responseCode = "404", description = "경매를 찾을 수 없음")
    @ApiResponse(responseCode = "403", description = "해당 경매가 종료, 혹은 기존 입찰보다 낮은 입찰가인 경우")
    @PostMapping("/bids")
    @ResponseStatus(HttpStatus.CREATED)
    public void createBid(@Valid @RequestBody BidRequest request,
            @AuthenticationPrincipal Member member) {
        // 1. 로그인한 유저 정보 취득
        // (현재는 `RequestParam`으로 처리하고 있으나 추후에 로그인 유저 정보에서 취득할 예정)

        // 2. 입찰 생성 호출
        bidService.createBid(member.getMemberId(), request);
    }

    @Operation(summary = "입찰 취소")
    @ApiResponse(responseCode = "204", description = "입찰 취소 성공")
    @ApiResponse(responseCode = "404", description = "경매중인 경매 또는 입찰을 찾을 수 없음")
    @DeleteMapping("/bids/{bidId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBid(@PathVariable("bidId") long bidId,
            @AuthenticationPrincipal Member member) {
        // 1. 로그인한 유저 정보 취득
        // (현재는 `RequestParam`으로 처리하고 있으나 추후에 로그인 유저 정보에서 취득할 예정)

        // 2. 입찰 취소 호출
        bidService.deleteBid(member.getMemberId(), bidId);
    }

}
