package com.deal4u.fourplease.domain.order.mapper;

import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.entity.Order;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderMapper {

    public static OrderResponse toOrderResponse(Order order) {
        Address address = order.getAddress();

        return new OrderResponse(
                address.address(),
                address.addressDetail(),
                address.zipCode(),
                order.getPhone(),
                order.getContent(),
                order.getReceiver()
        );
    }
}
