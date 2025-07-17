package com.deal4u.fourplease.domain.settlement.controller;

import com.deal4u.fourplease.domain.settlement.service.SettlementService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SettlementController {

    private final SettlementService settlementService;

    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차상위 입찰자에게 입찰 제안 성공"),
            @ApiResponse(responseCode = "404", description = "해당 경매, 입찰 내역 또는 차상위 입찰자가 없거나"),
            @ApiResponse(responseCode = "409", description = "이미 차상위 입찰자에게 정산을 시도하거나 이미 정산이 존재합니다.")
    })
    @PostMapping("/auctions/{auctionId}/second-bidder/offer")
    public void offerSecondBidder(
            @PathVariable @Positive Long auctionId) {
        settlementService.offerSecondBidder(auctionId);
    }
}
