package com.deal4u.fourplease.domain.auction.mapper;

import com.deal4u.fourplease.domain.auction.dto.AuctionImageUrlResponse;
import java.net.URL;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AutionImageUrlMapper {

    public static AuctionImageUrlResponse toImageUrlResponse(URL auctionImageUrl) {
        return new AuctionImageUrlResponse(auctionImageUrl.toString());
    }
}
