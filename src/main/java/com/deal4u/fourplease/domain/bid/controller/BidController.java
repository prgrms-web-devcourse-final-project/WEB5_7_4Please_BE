package com.deal4u.fourplease.domain.bid.controller;

import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.dto.BidResponse;
import com.deal4u.fourplease.domain.bid.entity.PageResponse;
import com.deal4u.fourplease.domain.bid.service.BidService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
public class BidController {

    private final BidService bidService;

    /**
     * 특정 경매에 대한 입찰 목록을 페이징하여 조회합니다. 정렬 순서는 서비스 로직 상에서 고정하였습니다. (bidPrice DESC, bidTime ASC)
     *
     * @param auctionId 경매 ID
     * @param pageable  `Client`에서 요청한 Paging 정보 (e.g., ?page=0&size=10) 상기의 내용의 경우, `Page`와
     *                  `Slice`중 어느쪽으로 할지 협의가 필요한 부분입니다. `totalElements`를 화면 상에서 필요로 하기 때문에, `Page`로
     *                  우선 구현하였습니다.
     * @return Paging 처리 된 입찰 목록
     */
    @GetMapping("/auctions/{auctionId}/bids")
    @ResponseStatus(HttpStatus.OK)
    public List<BidResponse> getBids(@PathVariable Long auctionId,
            @PageableDefault Pageable pageable) {
        // 1. 일찰 내역 조회 호출
        PageResponse<BidResponse> bidPage = bidService.getBidListForAuction(auctionId, pageable);
        return bidPage.getContent();
    }

    @PostMapping("/bids")
    @ResponseStatus(HttpStatus.OK)
    public void createBid(@Valid @RequestBody BidRequest request,
            @RequestParam("memberId") Long memberId) {
        // 1. 로그인한 유저 정보 취득
        // (현재는 `RequestParam`으로 처리하고 있으나 추후에 로그인 유저 정보에서 취득할 예정)

        // 2. 입찰 생성 호출
        bidService.createBid(memberId, request);
    }

    @DeleteMapping("/bids/{bidId}")
    public void deleteBid(@PathVariable("bidId") long bidId, @RequestParam("memberId") Long memberId) {
        // 1. 로그인한 유저 정보 취득
        // (현재는 `RequestParam`으로 처리하고 있으나 추후에 로그인 유저 정보에서 취득할 예정)

        // 2. 입찰 취소 호출
        bidService.deleteBid(memberId, bidId);
    }

}
