package com.deal4u.fourplease.domain.file.util;

import com.deal4u.fourplease.global.exception.ErrorCode;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class FileUtil {

    public static String getFileExtension(@Nullable String filename) {
        if (filename == null) {
            throw ErrorCode.INVALID_FILE.toException();
        }
        int lineIndex = filename.lastIndexOf('.');
        if (lineIndex == -1) {
            throw ErrorCode.INVALID_FILE.toException();
        }
        return filename.substring(lineIndex + 1);
    }
}
