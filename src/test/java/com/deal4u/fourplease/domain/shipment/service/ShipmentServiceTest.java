package com.deal4u.fourplease.domain.shipment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.auction.service.AuctionStatusService;
import com.deal4u.fourplease.domain.shipment.dto.TrackingNumberRequest;
import com.deal4u.fourplease.domain.shipment.entity.Shipment;
import com.deal4u.fourplease.domain.shipment.entity.ShipmentStatus;
import com.deal4u.fourplease.domain.shipment.repository.ShipmentRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private AuctionStatusService auctionStatusService;

    @InjectMocks
    private ShipmentService shipmentService;

    private Auction auction;
    private TrackingNumberRequest trackingNumberRequest;

    @BeforeEach
    void setUp() {
        auction = Auction.builder()
                .auctionId(1L)
                .build();

        trackingNumberRequest = new TrackingNumberRequest("1234567890");
    }

    @Nested
    class SaveShipmentTests {

        @Test
        @DisplayName("배송 정보 저장이 정상적으로 수행되는 경우")
        void testSaveShipmentSuccessful() {
            // Given
            Long auctionId = 1L;

            Shipment expectedShipment = Shipment.builder()
                    .auction(auction)
                    .shippingCode("1234567890")
                    .status(ShipmentStatus.INTRANSIT)
                    .build();

            when(auctionRepository.findById(auctionId))
                    .thenReturn(Optional.of(auction));
            when(shipmentRepository.save(any(Shipment.class)))
                    .thenReturn(expectedShipment);

            // When
            shipmentService.saveShipment(auctionId, trackingNumberRequest);

            // Then
            ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
            verify(shipmentRepository, times(1)).save(shipmentCaptor.capture());

            Shipment savedShipment = shipmentCaptor.getValue();
            assertThat(savedShipment.getAuction()).isEqualTo(auction);
            assertThat(savedShipment.getShippingCode()).isEqualTo("1234567890");
            assertThat(savedShipment.getStatus()).isEqualTo(ShipmentStatus.INTRANSIT);

            verify(auctionRepository, times(1)).findById(auctionId);
        }

        @Test
        @DisplayName("존재하지 않는 경매 ID로 배송 정보 저장 시 예외 발생")
        void testSaveShipmentAuctionNotFound() {
            // Given
            Long nonExistentAuctionId = 999L;

            when(auctionRepository.findById(nonExistentAuctionId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(
                    () -> shipmentService.saveShipment(nonExistentAuctionId, trackingNumberRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 경매를 찾을 수 없습니다.");

            verify(auctionRepository, times(1)).findById(nonExistentAuctionId);
            verify(shipmentRepository, times(0)).save(any(Shipment.class));
        }

        @Test
        @DisplayName("빈 배송 코드로 배송 정보 저장")
        void testSaveShipmentEmptyTrackingCode() {
            // Given
            Long auctionId = 1L;
            TrackingNumberRequest emptyTrackingRequest = new TrackingNumberRequest("");

            when(auctionRepository.findById(auctionId))
                    .thenReturn(Optional.of(auction));

            // When
            shipmentService.saveShipment(auctionId, emptyTrackingRequest);

            // Then
            ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
            verify(shipmentRepository, times(1)).save(shipmentCaptor.capture());

            Shipment savedShipment = shipmentCaptor.getValue();
            assertThat(savedShipment.getShippingCode()).isEmpty();

            verify(auctionRepository, times(1)).findById(auctionId);
        }
    }

    @Nested
    class ConfirmPurchaseTests {

        @Test
        @DisplayName("구매 확정이 정상적으로 수행되는 경우")
        void testConfirmPurchaseSuccessful() {
            // Given
            Long auctionId = 1L;

            Shipment shipment = Shipment.builder()
                    .shipmentId(1L)
                    .auction(auction)
                    .shippingCode("1234567890")
                    .status(ShipmentStatus.INTRANSIT)
                    .build();

            when(auctionRepository.findById(auctionId))
                    .thenReturn(Optional.of(auction));
            when(shipmentRepository.findByAuction(auction))
                    .thenReturn(Optional.of(shipment));

            // When
            shipmentService.confirmPurchase(auctionId);

            // Then
            assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
            verify(auctionRepository, times(1)).findById(auctionId);
            verify(shipmentRepository, times(1)).findByAuction(auction);
        }

        @Test
        @DisplayName("존재하지 않는 경매 ID로 구매 확정 시 예외 발생")
        void testConfirmPurchaseAuctionNotFound() {
            // Given
            Long nonExistentAuctionId = 999L;

            when(auctionRepository.findById(nonExistentAuctionId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shipmentService.confirmPurchase(nonExistentAuctionId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 경매를 찾을 수 없습니다.");

            verify(auctionRepository, times(1)).findById(nonExistentAuctionId);
            verify(shipmentRepository, times(0)).findByAuction(any());
        }

        @Test
        @DisplayName("배송 정보가 없는 경매에 대해 구매 확정 시 예외 발생")
        void testConfirmPurchaseShipmentNotFound() {
            // Given
            Long auctionId = 1L;

            when(auctionRepository.findById(auctionId))
                    .thenReturn(Optional.of(auction));
            when(shipmentRepository.findByAuction(auction))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shipmentService.confirmPurchase(auctionId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage("해당 배송 정보를 찾을 수 없습니다.");

            verify(auctionRepository, times(1)).findById(auctionId);
            verify(shipmentRepository, times(1)).findByAuction(auction);
        }
    }
}
