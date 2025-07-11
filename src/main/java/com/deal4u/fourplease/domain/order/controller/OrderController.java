package com.deal4u.fourplease.domain.order.controller;

import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/auctions/{auctionId}/orders/{type}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "주문이 성공적으로 생성됨"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 주문 타입이거나 낙찰자가 아님"),
            @ApiResponse(responseCode = "404", description = "경매나 사용자가 없음")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public String createOrder(
            @PathVariable @Positive Long auctionId,
            @PathVariable String type,
            @RequestBody OrderCreateRequest orderCreateRequest
    ) {
        return orderService.saveOrder(auctionId, type, orderCreateRequest);
    }

    @GetMapping("/orders/{orderId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public OrderResponse getOrder(@PathVariable @Positive Long orderId) {
        return orderService.getOrder(orderId);
    }
}
