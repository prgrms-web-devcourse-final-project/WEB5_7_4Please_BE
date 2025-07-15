package com.deal4u.fourplease.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.auction.factory.AuctionSaveDataFactory;
import com.deal4u.fourplease.domain.file.service.SaveData;
import com.deal4u.fourplease.domain.file.type.ImageType;
import org.junit.jupiter.api.Test;

class AuctionSaveDataFactoryTest {

    @Test
    void 경로생성() {
        AuctionSaveDataFactory auctionSaveDataFactory = new AuctionSaveDataFactory(
                value -> "/test",
                () -> "name"
        );

        SaveData saveData = auctionSaveDataFactory.create("", ImageType.JPG);

        assertThat(saveData).isEqualTo(new SaveData("/test", "name.jpg"));
    }
}