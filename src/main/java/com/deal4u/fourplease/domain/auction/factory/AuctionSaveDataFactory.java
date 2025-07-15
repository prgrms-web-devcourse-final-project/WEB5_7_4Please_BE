package com.deal4u.fourplease.domain.auction.factory;

import com.deal4u.fourplease.domain.auction.service.AuctionImageNameRule;
import com.deal4u.fourplease.domain.auction.service.AuctionPathRule;
import com.deal4u.fourplease.domain.file.service.SaveData;
import com.deal4u.fourplease.domain.file.type.ImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionSaveDataFactory {

    private final AuctionPathRule<String> auctionPathRule;
    private final AuctionImageNameRule auctionImageNameRule;

    public SaveData create(String nickName, ImageType imageType) {
        String path = auctionPathRule.createAuctionPath(nickName);
        String name = auctionImageNameRule.createAuctionPath();
        return new SaveData(path, imageType.convert(name));
    }
}
