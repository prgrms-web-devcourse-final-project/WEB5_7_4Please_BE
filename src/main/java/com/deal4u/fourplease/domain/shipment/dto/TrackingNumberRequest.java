package com.deal4u.fourplease.domain.shipment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TrackingNumberRequest(
        @NotNull(message = "운송장 번호는 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "운송장 번호는 알파벳과 숫자만 가능합니다.")
        String trackingNumber
) {
}
