package com.deal4u.fourplease.domain.file.service.s3;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileValidatorTest {

    private final FileValidator validator = new FileValidator();

    @Test
    @DisplayName("지원하지않는 contentType이면 예외발생")
    void throwsException_whenContentTypeIsNotSupported() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.png",
                "application/zip", // unsupported MIME type
                new byte[]{1, 2, 3}
        );

        ThrowableAssert.ThrowingCallable validFunction = () -> validator.validate(
                file.getOriginalFilename(),
                file);

        // when & then
        assertThatThrownBy(validFunction)
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_FILE.getMessage());
    }

    @Test
    @DisplayName("확장자와 MIME이 불일치하면 예외발생")
    void throwsException_whenExtensionDoesNotMatchContentType() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",     // extension is txt
                "image/png",    // MIME type is png
                new byte[]{1, 2}
        );

        ThrowableAssert.ThrowingCallable validFunction = () -> validator.validate(
                file.getOriginalFilename(),
                file);
        // when & then
        assertThatThrownBy(validFunction)
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_FILE.getMessage());
    }

    @Test
    @DisplayName("저장하려는 파일이름의 확장자가 다르면 예외")
    void throwsException_whenSavingFileExtensionDiffersFromActualMimeType() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.png",     // extension is txt
                "image/png",    // MIME type is png
                new byte[]{1, 2}
        );

        ThrowableAssert.ThrowingCallable validFunction = () -> validator.validate(
                "file.jpg",
                file);
        // when & then
        assertThatThrownBy(validFunction)
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_FILE.getMessage());
    }

    @Test
    @DisplayName("확장자와 MIME이 일치하면 정상 통과")
    void passesValidation_whenExtensionMatchesContentType() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.png",
                "image/png", // valid MIME type and extension
                new byte[]{1, 2}
        );

        // when & then
        assertThatCode(
                () -> validator.validate(file.getOriginalFilename(),
                        file)).doesNotThrowAnyException();
    }

}