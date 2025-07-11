package com.deal4u.fourplease.domain.auction.controller;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAuction(
            @Valid @RequestBody AuctionCreateRequest request,
            //TODO: @AuthenticationPrincipal 추가
            Member member
    ) {
        auctionService.save(request, member);
    }


}
