package com.deal4u.fourplease.domain.shipment.controller;

import com.deal4u.fourplease.domain.shipment.dto.TrackingNumberRequest;
import com.deal4u.fourplease.domain.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "운송장 번호 등록 성공"),
            @ApiResponse(responseCode = "404", description = "해당 경매를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (운송장 번호 형식 오류 등)")
    })
    @PostMapping("/auctions/{auctionId}/shipment")
    public void registerTrackingNumber(
            @PathVariable @Positive Long auctionId,
            @RequestBody @Valid TrackingNumberRequest trackingNumberRequest) {
        shipmentService.saveShipment(auctionId, trackingNumberRequest);
    }
}
