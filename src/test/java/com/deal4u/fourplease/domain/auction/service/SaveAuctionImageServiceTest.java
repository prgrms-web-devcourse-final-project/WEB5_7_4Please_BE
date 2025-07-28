package com.deal4u.fourplease.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deal4u.fourplease.domain.auction.dto.AuctionImageUrlResponse;
import com.deal4u.fourplease.domain.auction.factory.AuctionSaveDataFactory;
import com.deal4u.fourplease.domain.file.service.FileSaver;
import com.deal4u.fourplease.domain.file.service.SavePath;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class SaveAuctionImageServiceTest {

    @Test
    @DisplayName("경매 이미지 업로드 요청을 처리한다")
    void processesAuctionImageUploadRequest() {
        String path = "/test";
        String fileName = "test";
        AuctionSaveDataFactory saveDataFactory = new AuctionSaveDataFactory(mail -> path,
                () -> fileName);

        FakeFileSaver fakeFileSaver = new FakeFileSaver();
        SaveAuctionImageService saveAuctionImageService = new SaveAuctionImageService(
                saveDataFactory,
                fakeFileSaver, "test.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.png",
                "image/png", // valid MIME type and extension
                new byte[] {1, 2}
        );
        Member test = Member.builder().nickName("test").build();

        AuctionImageUrlResponse upload = saveAuctionImageService.upload(test, file);

        assertThat(new SavePath(path, fileName + ".png")).isEqualTo(
                fakeFileSaver.getInputSavePath());
        assertThat(file).isEqualTo(fakeFileSaver.getFile());
        assertThat(upload.url()).isEqualTo("https://test.com" + path);
    }

    @Test
    @DisplayName("이미지가 아니면 업로드 실패")
    void rejectsUploadWhenFileIsNotValidImage() {
        AuctionSaveDataFactory saveDataFactory = new AuctionSaveDataFactory(value -> "/test",
                () -> "test");

        SaveAuctionImageService saveAuctionImageService = new SaveAuctionImageService(
                saveDataFactory, new FakeFileSaver(), "test.com");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain", // valid MIME type and extension
                new byte[] {1, 2}
        );
        Member test = Member.builder().nickName("test").build();

        assertThatThrownBy(() -> saveAuctionImageService.upload(test, file))
                .isInstanceOf(GlobalException.class);
    }

    @Getter
    private static class FakeFileSaver implements FileSaver {

        private SavePath inputSavePath;
        private MultipartFile file;

        @Override
        public URL save(SavePath savePath, MultipartFile file) {
            this.inputSavePath = savePath;
            this.file = file;
            try {
                return URL.of(new URI("https", "example.com", savePath.filePath(), null), null);
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
