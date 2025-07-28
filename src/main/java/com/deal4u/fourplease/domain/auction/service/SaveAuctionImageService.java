package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionImageUrlResponse;
import com.deal4u.fourplease.domain.auction.factory.AuctionSaveDataFactory;
import com.deal4u.fourplease.domain.auction.mapper.AutionImageUrlMapper;
import com.deal4u.fourplease.domain.file.service.FileSaver;
import com.deal4u.fourplease.domain.file.service.SavePath;
import com.deal4u.fourplease.domain.file.type.ImageType;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SaveAuctionImageService {

    private final AuctionSaveDataFactory auctionSaveDataFactory;
    private final FileSaver fileSaver;

    private final String hostUrl;

    @Autowired
    public SaveAuctionImageService(AuctionSaveDataFactory auctionSaveDataFactory,
            FileSaver fileSaver,
            @Value("${deal4u.host-url}") String hostUrl) {
        this.auctionSaveDataFactory = auctionSaveDataFactory;
        this.fileSaver = fileSaver;
        this.hostUrl = hostUrl;
    }

    public AuctionImageUrlResponse upload(Member member, List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            ImageType imageType = ImageType.findTypeByStr(file.getOriginalFilename()).orElseThrow(
                    ErrorCode.INVALID_IMAGE_TYPE::toException);
            SavePath savePath = auctionSaveDataFactory.create(member.getNickName(), imageType);
            URL url = fileSaver.save(savePath, file);
            urls.add(changeHostUrl(url).toString());
        }

        return AutionImageUrlMapper.toImageUrlResponse(urls);
    }

    private URL changeHostUrl(URL url) {
        try {
            URI originalUri = url.toURI();
            URI modifiedUri = new URI(
                    originalUri.getScheme(),
                    hostUrl,
                    originalUri.getPath(),
                    originalUri.getQuery(),
                    originalUri.getFragment()
            );
            return modifiedUri.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw ErrorCode.DOES_MODIFIED_URL.toException();
        }
    }
}
