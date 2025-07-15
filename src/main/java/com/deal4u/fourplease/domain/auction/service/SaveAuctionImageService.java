package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionImageUrlResponse;
import com.deal4u.fourplease.domain.auction.factory.AuctionSaveDataFactory;
import com.deal4u.fourplease.domain.auction.mapper.AutionImageUrlMapper;
import com.deal4u.fourplease.domain.file.service.FileSaver;
import com.deal4u.fourplease.domain.file.service.SaveData;
import com.deal4u.fourplease.domain.file.type.ImageType;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SaveAuctionImageService {

    private final AuctionSaveDataFactory auctionSaveDataFactory;
    private final FileSaver fileSaver;

    public AuctionImageUrlResponse upload(Member member, MultipartFile file) {
        ImageType imageType = ImageType.findTypeByStr(file.getOriginalFilename()).orElseThrow(
                ErrorCode.ENTITY_NOT_FOUND::toException);
        SaveData saveData = auctionSaveDataFactory.create(member.getNickName(), imageType);
        return AutionImageUrlMapper.toImageUrlResponse(fileSaver.save(saveData, file));
    }
}
