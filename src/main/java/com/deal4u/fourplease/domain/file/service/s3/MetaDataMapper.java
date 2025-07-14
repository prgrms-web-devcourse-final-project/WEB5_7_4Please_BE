package com.deal4u.fourplease.domain.file.service.s3;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

@UtilityClass
public class MetaDataMapper {

    public static S3MetaData toMetaData(MultipartFile file) {
        return new S3MetaData(Objects.requireNonNull(file.getContentType()), file.getSize());
    }
}