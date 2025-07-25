package com.deal4u.fourplease.domain.auction.controller;

import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleSearchRequest;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sale", description = "판매내역 관리 API")
public class SaleController {

    private final AuctionService auctionService;

    @Operation(summary = "판매자 판매내역 조회")
    @ApiResponse(responseCode = "200", description = "판매자 판매 내역 조회 성공")
    @ApiResponse(responseCode = "404", description = "경매를 찾을 수 없음")
    @GetMapping("/{sellerId}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SellerSaleListResponse> getSales(
            @PathVariable(name = "sellerId") @Positive Long sellerId,
            @Valid @ModelAttribute @ParameterObject SellerSaleSearchRequest request
    ) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return auctionService.findSalesBySellerId(sellerId, pageable);
    }

}
