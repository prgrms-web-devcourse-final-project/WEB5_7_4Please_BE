package com.deal4u.fourplease.domain.file.service.s3;

import static com.deal4u.fourplease.domain.file.util.FileUtil.getFileExtension;

import com.deal4u.fourplease.domain.file.type.FileType;
import com.deal4u.fourplease.global.exception.ErrorCode;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    public void validate(String savedName, MultipartFile file) {
        if (file.isEmpty()) {
            throw ErrorCode.FILE_SAVE_FAILED.toException();
        }
        FileType fileType = validAndGetFileType(file);
        validSameFileType(file.getOriginalFilename(), fileType);
        validSameFileType(savedName, fileType);
    }

    private FileType validAndGetFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return FileType.findTypeByStr(contentType)
                    .orElseThrow(ErrorCode.INVALID_FILE::toException);
        }
        throw ErrorCode.INVALID_FILE.toException();
    }

    private void validSameFileType(@Nullable String fileName, FileType fileType) {
        String fileExtension = getFileExtension(fileName);
        if (!fileType.isTargetType(fileExtension)) {
            throw ErrorCode.INVALID_FILE.toException();
        }
    }
}
