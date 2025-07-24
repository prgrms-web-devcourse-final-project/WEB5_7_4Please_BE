package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.SaleAuctionStatus;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import com.deal4u.fourplease.domain.shipment.repository.ShipmentRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionSupportService {

    private final BidService bidService;
    private final SettlementRepository settlementRepository;
    private final ShipmentRepository shipmentRepository;

    // TODO: bidRepository가 아닌 bidService에서 불러오는 방식으로 수정 필요


    public Page<AuctionListResponse> getAuctionListResponses(Page<Auction> auctionPage) {
        return auctionPage
                .map(auction -> {
                    BidSummaryDto bidSummaryDto = bidService
                            .getBidSummaryDto(auction.getAuctionId());
                    return AuctionListResponse.toAuctionListResponse(
                            auction,
                            bidSummaryDto,
                            // TODO: wishList 개발 후 수정 필요
                            false
                    );
                });
    }

    // TODO: 추후 개선 필요
    public SaleAuctionStatus getSaleAuctionStatus(Auction auction) {
        if (auction.getStatus().equals(AuctionStatus.OPEN)) {
            return SaleAuctionStatus.OPEN;
        }
        if (auction.getStatus().equals(AuctionStatus.FAIL)) {
            return SaleAuctionStatus.FAIL;
        }
        // AuctionStatus.CLOSE
        return getSettlementStatus(auction.getAuctionId());
    }

    private SaleAuctionStatus getSettlementStatus(Long auctionId) {
        String settlementStatus = settlementRepository.getSettlementStatusByAuctionId(auctionId)
                .toString();
        if (settlementStatus.equals("PENDING")) {
            return SaleAuctionStatus.PENDING;
        }
        if (settlementStatus.equals("SUCCESS")) {
            return SaleAuctionStatus.SUCCESS;
        }
        if (settlementStatus.equals("REJECTED")) {
            return SaleAuctionStatus.REJECT;
        }

        return getShipmentStatus(auctionId);
    }

    private SaleAuctionStatus getShipmentStatus(Long auctionId) {
        String shipmentStatus = shipmentRepository.getShipmentStatusByAuctionId(auctionId)
                .toString();
        if (shipmentStatus.equals("INTRANSIT")) {
            return SaleAuctionStatus.INTRANSIT;
        }
        if (shipmentStatus.equals("DELIVERED")) {
            return SaleAuctionStatus.DELIVERED;
        }
        throw ErrorCode.STATUS_NOT_FOUND.toException();
    }
}
