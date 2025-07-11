package com.deal4u.fourplease.domain.bid.dto;

import com.deal4u.fourplease.domain.bid.entity.BidMessageStatus;

public record BidMessageResponse(
        BidMessageStatus bidMessageStatus,
        BidResponse bidResponse
) {

}
