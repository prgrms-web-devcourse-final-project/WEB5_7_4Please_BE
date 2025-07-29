package com.deal4u.fourplease.domain.order.mapper;

import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.entity.Order;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderMapper {

    public static OrderResponse toOrderResponse(Order order) {
        Product product = order.getAuction().getProduct();
        Address address = order.getAddress();

        return new OrderResponse(
                product.getThumbnailUrl(),
                product.getName(),
                order.getPrice().longValue(),
                product.getSeller().getMember().getNickName(),
                address.address(),
                address.addressDetail(),
                address.zipCode(),
                order.getPhone(),
                order.getContent(),
                order.getReceiver()
        );
    }
}
