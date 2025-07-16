package com.deal4u.fourplease.domain.file.service.s3;

import static com.deal4u.fourplease.domain.file.service.s3.MetaDataMapper.toMetaData;

import com.deal4u.fourplease.domain.file.service.FileSaver;
import com.deal4u.fourplease.domain.file.service.SaveData;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Slf4j
public class S3FileSaver implements FileSaver {

    private final FileValidator fileValidator;
    private final S3FileUploader fileUploader;

    @Override
    public URL save(SaveData saveData, MultipartFile file) {
        fileValidator.validate(saveData.savedFileName(), file);
        try (InputStream inputStream = file.getInputStream()) {
            S3MetaData metaData = toMetaData(file);
            return fileUploader.upload(inputStream, saveData.fullPath(), metaData);
        } catch (IOException e) {
            throw ErrorCode.FILE_SAVE_FAILED.toException();
        }
    }
}
