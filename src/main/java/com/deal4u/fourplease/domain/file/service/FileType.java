package com.deal4u.fourplease.domain.file.service;

import java.util.Optional;
import java.util.Set;
import org.springframework.lang.NonNull;

public enum FileType {
    JPEG("image/jpeg", Set.of("jpeg", "jpg")),
    PNG("image/png", Set.of("png")),
    GIF("image/gif", Set.of("gif")),
    ;

    private final String mimeType;
    private final Set<String> fileExtension;

    FileType(String mimeType, Set<String> fileExtension) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }

    public static Optional<FileType> findTypeByStr(@NonNull String contentType) {
        for (FileType type : values()) {
            if (type.mimeType.equalsIgnoreCase(contentType)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public boolean isTargetType(String targetFileExtension) {
        return fileExtension.contains(targetFileExtension);
    }
}
