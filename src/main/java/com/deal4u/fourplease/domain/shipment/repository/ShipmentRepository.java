package com.deal4u.fourplease.domain.shipment.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.shipment.entity.Shipment;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @Query("select s.status "
            + "from Shipment s "
            + "where s.auction.auctionId = :auctionId")
    ShipmentStatus getShipmentStatusByAuctionId(Long auctionId);

    Optional<Shipment> findByAuction(Auction auction);
}
