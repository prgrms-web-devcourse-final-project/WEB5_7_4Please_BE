package com.deal4u.fourplease.domain.file.type;

import com.deal4u.fourplease.domain.file.util.FileUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageType {
    JPEG("jpeg"),
    JPG("jpg"),
    PNG("png"),
    GIF("gif");

    private final String extension;

    public static Optional<ImageType> findTypeByStr(String fileName) {
        if (fileName == null) {
            return Optional.empty();
        }
        String fileExtension = FileUtil.getFileExtension(fileName);
        for (ImageType type : values()) {
            if (type.extension.equalsIgnoreCase(fileExtension)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public String convert(String name) {
        return name + "." + extension;
    }
}
