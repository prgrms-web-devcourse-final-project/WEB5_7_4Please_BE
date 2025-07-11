package com.deal4u.fourplease.domain.bid.controller;

import com.deal4u.fourplease.domain.bid.dto.BidRequest;
import com.deal4u.fourplease.domain.bid.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping("/bids")
    @ResponseStatus(HttpStatus.OK)
    public void createBid(@Valid @RequestBody BidRequest request) {
        // 1. 로그인한 유저 정보 취득 (현재는 하드 코딩)
        long memberId = 1L;

        // 2. 입찰 생성 호출
        bidService.createBid(memberId, request);
    }

    @DeleteMapping("/bids/{bidId}")
    public void deleteBid(@PathVariable long bidId) {
        // 1. 로그인한 유저 정보 취득 (현재는 하드 코딩)
        long memberId = 1L;


        bidService.deleteBid(memberId, bidId);


    }

}
