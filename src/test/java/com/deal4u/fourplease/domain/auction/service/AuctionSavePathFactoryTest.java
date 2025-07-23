package com.deal4u.fourplease.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.auction.factory.AuctionSaveDataFactory;
import com.deal4u.fourplease.domain.file.service.SavePath;
import com.deal4u.fourplease.domain.file.type.ImageType;
import org.junit.jupiter.api.Test;

class AuctionSavePathFactoryTest {

    @Test
    void 경로생성() {
        AuctionSaveDataFactory auctionSaveDataFactory = new AuctionSaveDataFactory(
                value -> "/test",
                () -> "name"
        );

        SavePath savePath = auctionSaveDataFactory.create("", ImageType.JPG);

        assertThat(savePath).isEqualTo(new SavePath("/test", "name.jpg"));
    }
}