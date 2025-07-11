package com.deal4u.fourplease.domain.file.service.s3;

import static com.deal4u.fourplease.domain.file.util.FileUtil.getFileExtension;

import com.deal4u.fourplease.domain.file.service.FileSaver;
import com.deal4u.fourplease.domain.file.service.SaveData;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Slf4j
public class S3FileSaver implements FileSaver {

    private final FileValidator fileValidator;
    private final S3FileUploader fileUploader;

    @Override
    public String save(SaveData saveData, MultipartFile file) {
        fileValidator.valid(file);
        try (InputStream inputStream = file.getInputStream()) {
            return fileUploader.upload(inputStream, getSavedPath(saveData, file),
                    getMetaData(file));
        } catch (IOException e) {
            throw ErrorCode.FILE_SAVE_FAILED.toException();
        }
    }

    private Map<String, String> getMetaData(MultipartFile file) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put("Content-Type", file.getContentType());
        metaData.put("Content-Length", String.valueOf(file.getSize()));
        return metaData;
    }

    private String getSavedPath(SaveData saveData, MultipartFile file) {
        return saveData.fullPath() + "." + getFileExtension(file.getOriginalFilename());
    }
}
