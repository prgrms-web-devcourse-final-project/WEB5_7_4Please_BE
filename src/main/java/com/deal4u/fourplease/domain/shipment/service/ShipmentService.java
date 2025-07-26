package com.deal4u.fourplease.domain.shipment.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.SHIPMENT_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.auction.service.AuctionStatusService;
import com.deal4u.fourplease.domain.shipment.dto.TrackingNumberRequest;
import com.deal4u.fourplease.domain.shipment.entity.Shipment;
import com.deal4u.fourplease.domain.shipment.mapper.ShipmentMapper;
import com.deal4u.fourplease.domain.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final AuctionStatusService auctionStatusService;
    private final ShipmentRepository shipmentRepository;
    private final AuctionRepository auctionRepository;

    public void saveShipment(Long auctionId, TrackingNumberRequest trackingNumberRequest) {
        Auction auction = getAuctionOrThrow(auctionId);

        Shipment shipment = ShipmentMapper.toEntity(auction, trackingNumberRequest);

        shipmentRepository.save(shipment);

        auctionStatusService.markAuctionAsInTransit(auction);
    }

    @Transactional
    public void confirmPurchase(Long auctionId) {
        Auction auction = getAuctionOrThrow(auctionId);

        Shipment shipment = getShipmentOrThrow(auction);

        shipment.updateStatusToDelivered();

        auctionStatusService.markAuctionAsDelivered(auction);
    }

    private Shipment getShipmentOrThrow(Auction auction) {
        return shipmentRepository.findByAuction(auction)
                .orElseThrow(SHIPMENT_NOT_FOUND::toException);
    }

    private Auction getAuctionOrThrow(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }
}
