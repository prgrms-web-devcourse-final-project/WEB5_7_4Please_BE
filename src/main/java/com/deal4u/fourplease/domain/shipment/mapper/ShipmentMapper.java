package com.deal4u.fourplease.domain.shipment.mapper;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.shipment.dto.TrackingNumberRequest;
import com.deal4u.fourplease.domain.shipment.entity.Shipment;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;

public class ShipmentMapper {

    private ShipmentMapper() {
        // private constructor to prevent instantiation
    }

    public static Shipment toEntity(Auction auction, TrackingNumberRequest trackingNumberRequest) {
        return Shipment.builder()
                .auction(auction)
                .shippingCode(trackingNumberRequest.trackingNumber())
                .status(ShipmentStatus.INTRANSIT)
                .build();
    }
}
