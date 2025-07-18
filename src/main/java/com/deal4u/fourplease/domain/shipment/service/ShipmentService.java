package com.deal4u.fourplease.domain.shipment.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.shipment.dto.TrackingNumberRequest;
import com.deal4u.fourplease.domain.shipment.entity.Shipment;
import com.deal4u.fourplease.domain.shipment.mapper.ShipmentMapper;
import com.deal4u.fourplease.domain.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final AuctionRepository auctionRepository;

    public void saveShipment(Long auctionId, TrackingNumberRequest trackingNumberRequest) {
        Auction auction = getAuctionOrThrow(auctionId);

        Shipment shipment = ShipmentMapper.toEntity(auction, trackingNumberRequest);

        shipmentRepository.save(shipment);
    }

    private Auction getAuctionOrThrow(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }
}
