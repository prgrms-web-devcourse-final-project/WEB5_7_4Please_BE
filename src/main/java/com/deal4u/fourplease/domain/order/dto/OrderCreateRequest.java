package com.deal4u.fourplease.domain.order.dto;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record OrderCreateRequest(

        @NotNull(message = "가격은 필수 입력 항목입니다.")
        @DecimalMin(value = "0.01", message = "가격은 0보다 커야 합니다.")
        Long price,

        @NotNull(message = "회원 ID는 필수 입력 항목입니다.")
        Long memberId
) {

    public static Order toEntity(OrderCreateRequest createRequest, Auction auction, Orderer orderer,
                                 OrderId orderId) {
        return Order.builder()
                .orderId(orderId)
                .orderer(orderer)
                .auction(auction)
                .price(new BigDecimal(createRequest.price))
                .build();
    }
}
