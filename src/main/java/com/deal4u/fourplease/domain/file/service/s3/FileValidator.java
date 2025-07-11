package com.deal4u.fourplease.domain.file.service.s3;

import static com.deal4u.fourplease.domain.file.util.FileUtil.getFileExtension;

import com.deal4u.fourplease.domain.file.service.FileType;
import com.deal4u.fourplease.global.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    public void valid(MultipartFile file) {
        FileType fileType = validAndGetFileType(file);
        validSameFileType(file, fileType);
    }

    private FileType validAndGetFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return FileType.findTypeByStr(contentType)
                    .orElseThrow(ErrorCode.INVALID_FILE::toException);
        }
        throw ErrorCode.INVALID_FILE.toException();
    }

    private void validSameFileType(MultipartFile file, FileType fileType) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!fileType.isTargetType(fileExtension)) {
            throw ErrorCode.INVALID_FILE.toException();
        }
    }
}
