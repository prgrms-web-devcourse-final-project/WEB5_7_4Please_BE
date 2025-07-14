package com.deal4u.fourplease.domain.auction.controller;

import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.bid.entity.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/{sellerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SellerSaleListResponse> getSales(@PathVariable Long sellerId) {
        return null;
    }

}
