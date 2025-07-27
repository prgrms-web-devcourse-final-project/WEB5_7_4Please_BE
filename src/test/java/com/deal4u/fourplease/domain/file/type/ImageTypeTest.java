package com.deal4u.fourplease.domain.file.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ImageTypeTest {

    @Test
    void imageSearch() {
        Optional<ImageType> typeByStr = ImageType.findTypeByStr("test.jpg");
        assertThat(typeByStr).isPresent().contains(ImageType.JPG);
    }
}
