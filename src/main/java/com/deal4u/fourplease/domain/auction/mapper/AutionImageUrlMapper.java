package com.deal4u.fourplease.domain.auction.mapper;

import com.deal4u.fourplease.domain.auction.dto.AuctionImageUrlResponse;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AutionImageUrlMapper {

    public static AuctionImageUrlResponse toImageUrlResponse(List<String> urls) {
        return new AuctionImageUrlResponse(urls);
    }
}
